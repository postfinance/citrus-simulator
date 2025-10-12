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

import org.citrusframework.message.Message;
import org.citrusframework.simulator.endpoint.SimulationFailedUnexpectedlyException;
import org.citrusframework.simulator.exception.SimulatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ScenarioEndpointTest {

    private ScenarioEndpoint fixture;

    @BeforeEach
    void beforeEachSetup() {
        fixture = new ScenarioEndpoint(null);
    }

    @Nested
    class Fail {

        @Test
        void doesNotThrowExceptionWhenNoResponseFutureIsPresent() {
            fixture.fail(null, null);
        }

        @Test
        void completesResponseFutureIfOneIsPresent() {
            var responseFuture = new CompletableFuture<Message>();
            var request = mock(Message.class);
            fixture.add(request, responseFuture);

            var cause = mock(Throwable.class);
            var testContext = new TestContext();
            fixture.receive(testContext);

            fixture.fail(cause, testContext);

            assertThat(responseFuture)
                .isCompleted();
            assertThat(responseFuture.join())
                .isInstanceOf(SimulationFailedUnexpectedlyException.class)
                .extracting(Message::getPayload)
                .isEqualTo(cause);
        }
    }
}
