package org.citrusframework.simulator.service.impl;

import au.com.dius.pact.consumer.dsl.HttpRequestBuilder;
import au.com.dius.pact.consumer.dsl.PactDslResponse;
import au.com.dius.pact.core.model.BasePact;
import au.com.dius.pact.core.model.Consumer;
import au.com.dius.pact.core.model.ContentType;
import au.com.dius.pact.core.model.Interaction;
import au.com.dius.pact.core.model.OptionalBody;
import au.com.dius.pact.core.model.OptionalBody.State;
import au.com.dius.pact.core.model.Pact;
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
import org.citrusframework.simulator.service.dto.PactDto;
import org.citrusframework.simulator.service.dto.RequestAndResponseDto;
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

import static org.citrusframework.simulator.model.Message.Direction.INBOUND;
import static org.citrusframework.simulator.model.Message.Direction.OUTBOUND;


@Service
@Transactional
public class PactServiceImpl implements PactService {
    private final MessageServiceImpl messageService;
    private final MessageRepository messageRepository;

    private final MessageQueryService messageQueryService;

    public PactServiceImpl(MessageServiceImpl messageService, MessageRepository messageRepository, MessageQueryService messageQueryService) {
        this.messageService = messageService;
        this.messageRepository = messageRepository;
        this.messageQueryService = messageQueryService;
    }


    @Override
    public List<RequestResponsePact> getPactData(MessageCriteria messageCriteria) {
        List<Message> messages = messageQueryService.findByCriteriaWithoutpageable(messageCriteria);

        var messagesByExecution = messages.stream().collect(Collectors.groupingBy(Message::getScenarioExecutionId)).values();

        messagesByExecution.forEach(list-> toRequestResponsePact(list));

        Stream<Map<Direction, List<Message>>> groupedByDirection = groupedMes.stream()
            .map(m -> m.stream().collect(Collectors.groupingBy(Message::getDirection)));

        return groupedByDirection.map(scenarioMessages -> {

            Provider provider = new Provider();
            Consumer consumer = new Consumer();
            Request request = new Request();
            Response response = new Response();
            RequestResponsePact pactA = new RequestResponsePact(provider, consumer, new ArrayList<>());
            RequestResponsePact pactB = new RequestResponsePact(provider, consumer,
                Collections.singletonList(new RequestResponseInteraction("Test Merge Interaction")));
            pactA.mergeInteractions(pactB.getInteractions());
        }).collect(Collectors.toList());
        new RequestResponseInteraction("asdf", Collections.emptyList(), ).re
}

    private RequestResponsePact toRequestResponsePact(List<Message> messageList) {
        messageList.sort(Comparator.comparing(AbstractAuditingEntity::getCreatedDate));
        RequestResponsePact pactB = new RequestResponsePact(null, null,
            Collections.singletonList(new RequestResponseInteraction("Test Merge Interaction")));

        Provider provider = null;
        Consumer consumer = null;

        List<Interaction> interactions = new ArrayList<>();

        List<Message> messagesToProcess = new ArrayList<>(messageList);
        while (messagesToProcess.isEmpty()) {
            Message inbound = getNextMessage(messagesToProcess, Direction.INBOUND);
            if (inbound == null) {
                handleError
                continue;
            }

            Message outbound = getNextMessage(messagesToProcess, OUTBOUND);
            if (outbound == null) {
                handleError
                continue;
            }

            Provider messageProvider = createProvider(outbound);
            if (provider == null) {
                provider = messageProvider;
            } else if (!Objects.equals(provider.getName(), messageProvider.getName()) {
                handleError
            }


            Provider messageConsumer = createConusmer(outbound);
            if (consumer == null) {
                consumer = messageConsumer;
            } else if (!Objects.equals(consumer.getName(), messageConsumer.getName()) {
                handleError
            }

            interactions.add(createInteraction(inbound, outbound));
        }
return new RequestResponsePact(provider, consumer, interactions);


    }

    private Provider createProvider(Message outbound) {
        return new Provider(getFirstHeader())
    }

    private RequestResponseInteraction createInteraction(Message inbound, Message outbound) {
        return new RequestResponseInteraction(
            "", Collections.emptyList(), createRequest(inbound), createResponse(outbound)
        );
    }

    private Response createResponse(Message outbound) {
        Map<String, List<String>> messageHeaders = outbound.getHeaders().stream().collect(Collectors.groupingBy(MessageHeader::getName, Collectors.mapping(MessageHeader::getValue, Collectors.toList())));
        return new Response(getStatusCode(messageHeaders),
            getFilteredHeaders(messageHeaders),
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

    private Request createRequest(Message inbound) {
        Map<String, List<String>> messageHeaders = inbound.getHeaders().stream().collect(Collectors.groupingBy(MessageHeader::getName, Collectors.mapping(MessageHeader::getValue, Collectors.toList())));
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
        return  new OptionalBody(state, content, ContentType.getUNKNOWN());
    }

    private Map<String, List<String>> getFilteredHeaders(Map<String, List<String>> messageHeaders) {
        return messageHeaders.entrySet().stream().filter(entry -> entry.getKey().startsWith("citrus_")).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Map<String, List<String>> toMapOfList(String source) {
        String[] split = source.split(",");

        Map<String, List<String>> mapOfList = new HashMap<>();
        for (String segment : split) {
            String[] keyValue = segment.split("=");
            String key = keyValue[0];
            List valueList = mapOfList.computeIfAbsent(key, k->new ArrayList<>());
            if (keyValue.length == 2)  {
                valueList.add(keyValue[1]);
            }
        }
        return mapOfList;
    }

    private String getFirstHeader(Map<String, List<String>> messageHeaders, String key) {
        return messageHeaders.get(key);
    }

    private String getMethod(Message inbound) {
        inbound.getHeaders().
    }

    private Message getNextMessage(List<Message> messagesToProcess, Direction direction) {
        return messagesToProcess.stream().filter(message -> message.getDirection() == direction).findFirst().orElse(null);
    }

}
