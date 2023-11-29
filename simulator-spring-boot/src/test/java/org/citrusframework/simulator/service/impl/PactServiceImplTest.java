package org.citrusframework.simulator.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import au.com.dius.pact.core.model.OptionalBody.State;
import au.com.dius.pact.core.model.RequestResponseInteraction;
import au.com.dius.pact.core.model.RequestResponsePact;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.citrusframework.simulator.model.Message;
import org.citrusframework.simulator.model.Message.Direction;
import org.citrusframework.simulator.model.ScenarioExecution;
import org.citrusframework.simulator.service.MessageQueryService;
import org.citrusframework.simulator.service.criteria.MessageCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class PactServiceImplTest {

    @Mock
    private MessageQueryService messageQueryServiceMock;

    private PactServiceImpl fixture;

    @BeforeEach
    void beforeEachSetup() {
        fixture = new PactServiceImpl(messageQueryServiceMock);
    }

    @Test
    void getPactDataOfSingleRequestResponse() {

        List<Message> messageList = new ArrayList<>();
        ScenarioExecution scenarioExecution = mock(ScenarioExecution.class);
        doReturn(1l).when(scenarioExecution).getExecutionId();

        messageList.add(createInboundMessage(scenarioExecution, 0));
        messageList.add(createOutboundMessage(scenarioExecution, 0));

        MessageCriteria messageCriteriaMock = mock(MessageCriteria.class);

        doReturn(messageList).when(messageQueryServiceMock)
            .findByCriteriaWithoutpageable(messageCriteriaMock);
        List<RequestResponsePact> pactData = fixture.getPactData(messageCriteriaMock);

        assertThat(pactData)
            .hasSize(1)
            .anySatisfy(requestResponsePact -> {
                assertThat(requestResponsePact.getProvider().getName()).isEqualTo("provider-a");
                assertThat(requestResponsePact.getConsumer().getName()).isEqualTo("consumer-a");
                assertThat(requestResponsePact.getInteractions()).hasSize(1)
                    .anySatisfy(interaction -> {
                        assertInstanceOf(RequestResponseInteraction.class, interaction);
                        RequestResponseInteraction requestResponseInteraction = (RequestResponseInteraction) interaction;
                        assertThat(requestResponseInteraction.getRequest()).satisfies(request -> {
                            assertThat(request.getBody().getState()).isEqualTo(State.PRESENT);
                            assertThat(request.getBody().getValue()).asString(StandardCharsets.UTF_8).isEqualTo(
                                "some inbound payload");
                            assertThat(request.getMethod()).isEqualTo(
                                "GET");
                            assertThat(request.getPath()).isEqualTo(
                                "/api/v2/test");
                            assertThat(request.getQuery()).hasSize(2)
                                .anySatisfy((key, list) -> {
                                    assertThat(key).isEqualTo("p1");
                                    assertThat(list).contains("v1");
                                })
                                .anySatisfy((key, list) -> {
                                    assertThat(key).isEqualTo("p2");
                                    assertThat(list).contains("v2");
                                });
                            assertThat(request.getHeaders()).hasSize(1)
                                .anySatisfy((key, list) -> {
                                        assertThat(key).isEqualTo("ih1");
                                        assertThat(list).contains("iv1");
                                    }
                                ).hasSize(1);
                            assertThat(requestResponseInteraction.getResponse()).satisfies(
                                response -> {
                                    assertThat(response.getBody().getState()).isEqualTo(
                                        State.PRESENT);
                                    assertThat(response.getBody().getValue()).asString(
                                        StandardCharsets.UTF_8).isEqualTo(
                                        "some outbound payload");
                                    assertThat(response.getHeaders()).hasSize(1)
                                        .anySatisfy((key, list) -> {
                                                assertThat(key).isEqualTo("oh1");
                                                assertThat(list).contains("ov1");
                                            }
                                        ).hasSize(1);
                                });
                        });
                    });

            });
    }

        private Message createInboundMessage (ScenarioExecution scenarioExecution,long timeOffset){
            Map<String, Object> headers = new HashMap<>();
            headers.put("citrus_http_method", "GET");
            headers.put("citrus_http_path", "/api/v2/test");
            headers.put("citrus_http_query", "p1=v1,p2=v2");
            headers.put("ih1", "iv1");

            Message message = Message.builder().direction(Direction.INBOUND)
                .messageId((long) (Math.random() * 100000000))
                .createdDate(LocalDateTime.now().plusSeconds(timeOffset).toInstant(ZoneOffset.UTC))
                .headers(headers)
                .payload("some inbound payload").build();
            message.setScenarioExecution(scenarioExecution);
            return message;
        }

        private Message createOutboundMessage (ScenarioExecution scenarioExecution,long timeOffset){
            Map<String, Object> headers = new HashMap<>();
            headers.put("citrus_pact_provider", "provider-a");
            headers.put("citrus_pact_consumer", "consumer-a");
            headers.put("oh1", "ov1");

            Message message = Message.builder().direction(Direction.OUTBOUND)
                .messageId((long) (Math.random() * 100000000))
                .createdDate(LocalDateTime.now().plusSeconds(timeOffset).toInstant(ZoneOffset.UTC))
                .headers(headers)
                .payload("some outbound payload").build();
            message.setScenarioExecution(scenarioExecution);
            return message;
        }
    }
