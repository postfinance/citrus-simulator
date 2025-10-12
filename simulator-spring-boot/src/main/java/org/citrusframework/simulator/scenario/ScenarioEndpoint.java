/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.simulator.scenario;

import jakarta.annotation.Nullable;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.citrusframework.simulator.endpoint.EndpointMessageHandler;
import org.citrusframework.simulator.endpoint.SimulationFailedUnexpectedlyException;
import org.citrusframework.simulator.exception.SimulatorException;

import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Thread.currentThread;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ScenarioEndpoint extends AbstractEndpoint implements Producer, Consumer {

    private static final String RESPONSE_FUTURE_VARIABLE_NAME = "scenario.response.future";

    /**
     * Internal in memory message channel
     */
    private final LinkedBlockingQueue<Message> channel = new LinkedBlockingQueue<>();

    /**
     * Default constructor using endpoint configuration.
     *
     * @param endpointConfiguration
     */
    public ScenarioEndpoint(ScenarioEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    /**
     * Adds new message for direct message consumption.
     */
    public void add(Message request, CompletableFuture<Message> future) {
        request.setHeader(RESPONSE_FUTURE_VARIABLE_NAME, future);
        channel.add(request);
    }

    @Override
    public Producer createProducer() {
        return this;
    }

    @Override
    public Consumer createConsumer() {
        return this;
    }

    @Override
    public Message receive(TestContext context) {
        return receive(context, getEndpointConfiguration().getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        try {
            Message message = channel.poll(timeout, MILLISECONDS);

            if (isNull(message)) {
                throw new SimulatorException("Failed to receive scenario inbound message");
            }

            Object responseFuture = message.getHeader(RESPONSE_FUTURE_VARIABLE_NAME);
            if (responseFuture instanceof CompletableFuture) {
                context.setVariable(RESPONSE_FUTURE_VARIABLE_NAME, responseFuture);
            }

            messageReceived(message, context);

            return message;
        } catch (InterruptedException e) {
            currentThread().interrupt();
            throw new SimulatorException(e);
        }
    }

    @Override
    public void send(Message message, TestContext context) {
        messageSent(message, context);
        completeResponseFuture(message, context);
    }

    void fail(Throwable e, @Nullable TestContext context) {
        if (nonNull(context)) {
            completeResponseFuture(new SimulationFailedUnexpectedlyException(e), context);
        }
    }

    private void completeResponseFuture(Message message, TestContext context) {
        Object responseFuture = context.getVariable(RESPONSE_FUTURE_VARIABLE_NAME);

        if (responseFuture instanceof CompletableFuture) {
            ((CompletableFuture<Message>) responseFuture).complete(message);
        } else {
            throw new SimulatorException("Failed to process scenario response message - missing response consumer!");
        }
    }

    private void messageSent(Message message, TestContext context) {
        getEndpointMessageHandler(context).handleSentMessage(message, context);
    }

    private void messageReceived(Message message, TestContext context) {
        getEndpointMessageHandler(context).handleReceivedMessage(message, context);
    }

    private EndpointMessageHandler getEndpointMessageHandler(TestContext context) {
        return context.getReferenceResolver().resolve(EndpointMessageHandler.class);
    }
}
