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

package org.citrusframework.simulator.ws;

import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

/**
 * @author Christoph Deppisch
 */
@ExtendWith(MockitoExtension.class)
class WsdlScenarioGeneratorTest {

    private static final String SCENARIO_INPUT = String.format("<v1:TestRequest name=\"string\" id=\"100\" flag=\"false\" xmlns:v1=\"http://www.citrusframework.org/schema/samples/TestService/v1\">%n" +
        "  <v1:name>string</v1:name>%n" +
        "  <v1:id>100</v1:id>%n" +
        "  <v1:flag>true</v1:flag>%n" +
        "  <v1:restricted>stringstri</v1:restricted>%n" +
        "</v1:TestRequest>");

    private static final String SCENARIO_OUTPUT = String.format("<v1:TestResponse name=\"string\" id=\"100\" flag=\"false\" xmlns:v1=\"http://www.citrusframework.org/schema/samples/TestService/v1\">%n" +
        "  <v1:name>string</v1:name>%n" +
        "  <v1:id>100</v1:id>%n" +
        "  <v1:flag>true</v1:flag>%n" +
        "  <v1:restricted>stringstri</v1:restricted>%n" +
        "</v1:TestResponse>");
    @Mock
    private ConfigurableListableBeanFactory beanFactoryMock;
    @Mock
    private DefaultListableBeanFactory beanRegistryMock;
    private WsdlScenarioGenerator fixture;

    static Stream<Arguments> testGenerateScenarios() {
        return data();
    }

    static Stream<Arguments> testGenerateScenariosWithRegistry() {
        return data();
    }

    static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of("TestRequest", WsdlScenarioGenerator.WsdlScenarioNamingStrategy.INPUT, "/TestService/test", SCENARIO_INPUT, SCENARIO_OUTPUT),
            Arguments.of("test", WsdlScenarioGenerator.WsdlScenarioNamingStrategy.OPERATION, "/TestService/test", SCENARIO_INPUT, SCENARIO_OUTPUT),
            Arguments.of("/TestService/test", WsdlScenarioGenerator.WsdlScenarioNamingStrategy.SOAP_ACTION, "/TestService/test", SCENARIO_INPUT, SCENARIO_OUTPUT)
        );
    }

    @BeforeEach
    void beforeEachSetup() {
        fixture = new WsdlScenarioGenerator(new Resources.ClasspathResource("schema/TestService.wsdl"));
    }

    @MethodSource
    @ParameterizedTest
    void testGenerateScenarios(String scenarioName, WsdlScenarioGenerator.WsdlScenarioNamingStrategy namingStrategy, String soapAction, String input, String output) {
        fixture.setNamingStrategy(namingStrategy);

        doAnswer(invocation -> {
            WsdlOperationScenario scenario = (WsdlOperationScenario) invocation.getArguments()[1];

            assertEquals(scenario.getSoapAction(), soapAction);
            assertEquals(scenario.getInput(), input);
            assertEquals(scenario.getOutput(), output);

            return null;
        }).when(beanFactoryMock).registerSingleton(eq(scenarioName), any(WsdlOperationScenario.class));

        fixture.postProcessBeanFactory(beanFactoryMock);

        verify(beanFactoryMock).registerSingleton(eq(scenarioName), any(WsdlOperationScenario.class));
    }

    @MethodSource
    @ParameterizedTest
    void testGenerateScenariosWithRegistry(String scenarioName, WsdlScenarioGenerator.WsdlScenarioNamingStrategy namingStrategy, String soapAction, String input, String output) {
        fixture.setNamingStrategy(namingStrategy);

        doAnswer(invocation -> {
            BeanDefinition scenario = (BeanDefinition) invocation.getArguments()[1];

            assertEquals(scenario.getPropertyValues().get("soapAction"), soapAction);
            assertEquals(scenario.getPropertyValues().get("input"), input);
            assertEquals(scenario.getPropertyValues().get("output"), output);
            assertNull(scenario.getPropertyValues().get("inboundDataDictionary"));
            assertNull(scenario.getPropertyValues().get("outboundDataDictionary"));

            return null;
        }).when(beanRegistryMock).registerBeanDefinition(eq(scenarioName), any(BeanDefinition.class));

        fixture.postProcessBeanFactory(beanRegistryMock);

        verify(beanRegistryMock).registerBeanDefinition(eq(scenarioName), any(BeanDefinition.class));
    }

    @Test
    void generateScenariosWithDataDictionaries() {
        doReturn(true).when(beanRegistryMock).containsBeanDefinition("inboundXmlDataDictionary");
        doReturn(true).when(beanRegistryMock).containsBeanDefinition("outboundXmlDataDictionary");

        doAnswer(invocation -> {
            BeanDefinition scenario = (BeanDefinition) invocation.getArguments()[1];

            assertEquals(scenario.getPropertyValues().get("soapAction"), "/TestService/test");
            assertEquals(scenario.getPropertyValues().get("input"), SCENARIO_INPUT);
            assertEquals(scenario.getPropertyValues().get("output"), SCENARIO_OUTPUT);
            assertNotNull(scenario.getPropertyValues().get("inboundDataDictionary"));
            assertNotNull(scenario.getPropertyValues().get("outboundDataDictionary"));

            return null;
        }).when(beanRegistryMock).registerBeanDefinition(eq("TestRequest"), any(BeanDefinition.class));

        fixture.postProcessBeanFactory(beanRegistryMock);

        verify(beanRegistryMock).registerBeanDefinition(eq("TestRequest"), any(BeanDefinition.class));
    }
}
