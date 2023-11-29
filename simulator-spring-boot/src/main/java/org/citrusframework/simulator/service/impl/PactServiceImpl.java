package org.citrusframework.simulator.service.impl;

import au.com.dius.pact.core.model.Consumer;
import au.com.dius.pact.core.model.ContentType;
import au.com.dius.pact.core.model.Interaction;
import au.com.dius.pact.core.model.OptionalBody;
import au.com.dius.pact.core.model.OptionalBody.State;
import au.com.dius.pact.core.model.Provider;
import au.com.dius.pact.core.model.Request;
import au.com.dius.pact.core.model.RequestResponseInteraction;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.Response;
import org.citrusframework.simulator.model.AbstractAuditingEntity;
import org.citrusframework.simulator.model.Message;
import org.citrusframework.simulator.model.Message.Direction;
import org.citrusframework.simulator.model.MessageHeader;
import org.citrusframework.simulator.repository.MessageRepository;
import org.citrusframework.simulator.service.MessageQueryService;
import org.citrusframework.simulator.service.PactService;
import org.citrusframework.simulator.service.criteria.MessageCriteria;
import org.citrusframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.util.CollectionUtils;

import static org.citrusframework.simulator.model.Message.Direction.OUTBOUND;


@Service
@Transactional
public class PactServiceImpl implements PactService {


    private final MessageQueryService messageQueryService;

    public PactServiceImpl(
        MessageQueryService messageQueryService) {
        this.messageQueryService = messageQueryService;
    }


    @Override
    public List<RequestResponsePact> getPactData(MessageCriteria messageCriteria) {
        List<Message> messages = messageQueryService.findByCriteriaWithoutpageable(messageCriteria);

        var messagesByExecution = messages.stream()
            .collect(Collectors.groupingBy(Message::getScenarioExecutionId)).values();

        return messagesByExecution.stream().map(list -> toRequestResponsePact(list)).collect(Collectors.toList());
    }

    private RequestResponsePact toRequestResponsePact(List<Message> messageList) {
        messageList.sort(Comparator.comparing(AbstractAuditingEntity::getCreatedDate));

        Provider provider = null;
        Consumer consumer = null;

        List<Interaction> interactions = new ArrayList<>();

        List<Message> messagesToProcess = new ArrayList<>(messageList);
        while (!messagesToProcess.isEmpty()) {
            Message inbound = getNextMessage(messagesToProcess, Direction.INBOUND);
            Map<String, List<String>> inboundMessageHeaders = null;
            if (inbound != null) {
                inboundMessageHeaders = inbound.getHeaders().stream().collect(
                    Collectors.groupingBy(MessageHeader::getName,
                        Collectors.mapping(MessageHeader::getValue, Collectors.toList())));
            }

            Message outbound = getNextMessage(messagesToProcess, OUTBOUND);
            Map<String, List<String>> outboundMessageHeaders = null;
            if (outbound != null) {
                outboundMessageHeaders = outbound.getHeaders().stream()
                    .collect(Collectors.groupingBy(MessageHeader::getName,
                        Collectors.mapping(MessageHeader::getValue, Collectors.toList())));
            }

            Provider messageProvider = createProvider(outboundMessageHeaders);
            if (provider == null) {
                provider = messageProvider;
            } else if (!Objects.equals(provider.getName(), messageProvider.getName())) {
                provider = new Provider("inconsistent providers in messages");
            }

            Consumer messageConsumer = createConsumer(outboundMessageHeaders);
            if (consumer == null) {
                consumer = messageConsumer;
            } else if (!Objects.equals(consumer.getName(), messageConsumer.getName())) {
                consumer = new Consumer("inconsistent consumers in messages");
            }

            interactions.add(createInteraction(inbound, inboundMessageHeaders, outbound,
                outboundMessageHeaders));
        }
        return new RequestResponsePact(provider, consumer, interactions);


    }

    private Consumer createConsumer(Map<String, List<String>> outboundHeaders) {
        return new Consumer(getFirstHeader(outboundHeaders, "citrus_pact_consumer", "<unknown consumer>"));
    }

    private Provider createProvider(Map<String, List<String>> outboundHeaders) {
        return new Provider(getFirstHeader(outboundHeaders, "citrus_pact_provider", "<unknown provider>"));
    }

    private RequestResponseInteraction createInteraction(Message inbound,
        Map<String, List<String>> inboundMessageHeaders, Message outbound,
        Map<String, List<String>> outboundMessageHeaders) {
        return new RequestResponseInteraction(
            "", Collections.emptyList(), createRequest(inbound, inboundMessageHeaders),
            createResponse(outbound, outboundMessageHeaders)
        );
    }

    private Response createResponse(Message outbound,
        Map<String, List<String>> outboundMessageHeaders) {
        return new Response(getStatusCode(outboundMessageHeaders),
            getFilteredHeaders(outboundMessageHeaders),
            toOptionalBody(outbound));
    }

    private int getStatusCode(Map<String, List<String>> messageHeaders) {
        String citrusHttpStatusCode = getFirstHeader(messageHeaders, "citrus_http_status_code");
        if (!StringUtils.hasText(citrusHttpStatusCode)) {
            return -1;
        }

        try {
            return Integer.parseInt(citrusHttpStatusCode);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Request createRequest(Message inbound, Map<String, List<String>> messageHeaders) {
        if (inbound == null || messageHeaders == null) {
            return new Request();
        }

        return new Request(getFirstHeader(messageHeaders, "citrus_http_method"),
            getFirstHeader(messageHeaders, "citrus_http_path"),
            toMapOfList(getFirstHeader(messageHeaders, "citrus_http_query")),
            getFilteredHeaders(messageHeaders),
            toOptionalBody(inbound));
    }

    private OptionalBody toOptionalBody(Message inbound) {
        State state = State.NULL;
        byte[] content = new byte[0];
        if (StringUtils.hasText(inbound.getPayload())) {
            state = State.PRESENT;
            content = inbound.getPayload().getBytes(StandardCharsets.UTF_8);
        }
        // TODO Check type
        return new OptionalBody(state, content, ContentType.getUNKNOWN());
    }

    private Map<String, List<String>> getFilteredHeaders(Map<String, List<String>> messageHeaders) {
        return messageHeaders.entrySet().stream()
            .filter(entry -> !entry.getKey().startsWith("citrus_"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Map<String, List<String>> toMapOfList(String source) {
        String[] split = source.split(",");

        Map<String, List<String>> mapOfList = new HashMap<>();
        for (String segment : split) {
            String[] keyValue = segment.split("=");
            String key = keyValue[0];
            List valueList = mapOfList.computeIfAbsent(key, k -> new ArrayList<>());
            if (keyValue.length == 2) {
                valueList.add(keyValue[1]);
            }
        }
        return mapOfList;
    }

    private String getFirstHeader(Map<String, List<String>> messageHeaders, String key) {
        return getFirstHeader(messageHeaders, key, null);
    }

    private String getFirstHeader(Map<String, List<String>> messageHeaders, String key, String defaultValue) {
        List<String> values = messageHeaders.get(key);
        if (!CollectionUtils.isEmpty(values)) {
            return values.get(0);
        }
        return defaultValue;
    }


    private Message getNextMessage(List<Message> messagesToProcess, Direction direction) {
        Message nextMessage = messagesToProcess.stream()
            .filter(message -> message.getDirection() == direction)
            .findFirst().orElse(null);

        if (nextMessage != null) {
            messagesToProcess.remove(nextMessage);
        }

        return nextMessage;
    }

}
