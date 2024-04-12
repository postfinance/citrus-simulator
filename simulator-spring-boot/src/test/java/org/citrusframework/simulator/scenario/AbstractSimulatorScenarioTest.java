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

import org.citrusframework.TestCaseRunner;
import org.citrusframework.simulator.correlation.CorrelationHandlerBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class})
class AbstractSimulatorScenarioTest {

    @Mock
    private TestCaseRunner testCaseRunnerMock;

    @Mock
    private ScenarioEndpoint scenarioEndpointMock;

    @Test
    void isTestCaseRunnerAware() {
        var fixture = new AbstractSimulatorScenario() {
        };

        fixture.setTestCaseRunner(testCaseRunnerMock);

        assertThat(fixture.getTestCaseRunner())
            .isEqualTo(testCaseRunnerMock);
    }

    @Test
    void isScenarioEndpointAware() {
        var fixture = new AbstractSimulatorScenario() {
        };

        fixture.setScenarioEndpoint(scenarioEndpointMock);

        assertThat(fixture.getScenarioEndpoint())
            .isEqualTo(scenarioEndpointMock);
    }

    @Test
    void providesCorrelationHandler() {
        var fixture = new AbstractSimulatorScenario() {
        };
        fixture.setScenarioEndpoint(scenarioEndpointMock);

        assertThat(fixture.correlation())
            .isInstanceOf(CorrelationHandlerBuilder.class)
            .hasFieldOrPropertyWithValue("scenarioEndpoint", scenarioEndpointMock);
    }

    @Nested
    class AfterPropertiesSet {

        @Test
        void initializesScenarioEndpointIfNull() {
            var fixture = new AbstractSimulatorScenario() {
            };

            assertThat(fixture.getScenarioEndpoint())
                .isNull();

            fixture.afterPropertiesSet();

            assertThat(fixture.getScenarioEndpoint())
                .isNotNull()
                .isInstanceOf(DefaultScenarioEndpoint.class);
        }

        @Test
        void doesNothingIfScenarioEndpointAlreadyInitialized() {
            var fixture = new AbstractSimulatorScenario() {
            };
            fixture.setScenarioEndpoint(scenarioEndpointMock);

            fixture.afterPropertiesSet();

            assertThat(fixture.getScenarioEndpoint())
                .isEqualTo(scenarioEndpointMock);
        }
    }
}
