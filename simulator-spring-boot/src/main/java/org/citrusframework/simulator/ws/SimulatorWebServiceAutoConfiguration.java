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

import jakarta.annotation.Nullable;
import lombok.Getter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.EmptyResponseEndpointAdapter;
import org.citrusframework.simulator.SimulatorAutoConfiguration;
import org.citrusframework.simulator.config.SimulatorConfigurationProperties;
import org.citrusframework.simulator.correlation.CorrelationHandlerRegistry;
import org.citrusframework.simulator.endpoint.SimulatorEndpointAdapter;
import org.citrusframework.simulator.scenario.mapper.ContentBasedXPathScenarioMapper;
import org.citrusframework.simulator.scenario.mapper.ScenarioMapper;
import org.citrusframework.simulator.service.ScenarioExecutorService;
import org.citrusframework.ws.interceptor.LoggingEndpointInterceptor;
import org.citrusframework.ws.server.WebServiceEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.ws.config.annotation.WsConfigurationSupport;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.server.endpoint.adapter.MessageEndpointAdapter;
import org.springframework.ws.server.endpoint.mapping.UriEndpointMapping;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.addAll;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PROTECTED;

@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(SimulatorAutoConfiguration.class)
@Import({SimulatorWebServiceLoggingAutoConfiguration.class})
@EnableConfigurationProperties(SimulatorWebServiceConfigurationProperties.class)
@ConditionalOnProperty(prefix = "citrus.simulator.ws", value = "enabled", havingValue = "true")
public class SimulatorWebServiceAutoConfiguration {

    public static final String WS_ENDPOINT_ADAPTER_BEAN_NAME = "simulatorWsEndpointAdapter";

    @Getter(PROTECTED)
    private final LoggingEndpointInterceptor loggingEndpointInterceptor;

    @Getter(PROTECTED)
    private final SimulatorWebServiceConfigurationProperties simulatorWebServiceConfiguration;

    @Getter(PROTECTED)
    private final @Nullable SimulatorWebServiceConfigurer configurer;

    public SimulatorWebServiceAutoConfiguration(LoggingEndpointInterceptor loggingEndpointInterceptor, SimulatorWebServiceConfigurationProperties simulatorWebServiceConfiguration, @Autowired(required = false) @Nullable SimulatorWebServiceConfigurer configurer) {
        this.loggingEndpointInterceptor = loggingEndpointInterceptor;
        this.simulatorWebServiceConfiguration = simulatorWebServiceConfiguration;
        this.configurer = configurer;
    }

    @Bean
    public MessageEndpointAdapter messageEndpointAdapter() {
        return new MessageEndpointAdapter();
    }

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> simulatorServletRegistrationBean(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, getServletMapping());
    }

    @Bean
    public EndpointMapping simulatorWsEndpointMapping(MessageEndpoint simulatorWsEndpoint) {
        UriEndpointMapping endpointMapping = new UriEndpointMapping();
        endpointMapping.setOrder(Ordered.HIGHEST_PRECEDENCE);

        endpointMapping.setDefaultEndpoint(simulatorWsEndpoint);
        endpointMapping.setInterceptors(interceptors());

        return endpointMapping;
    }

    @Bean
    public MessageEndpoint simulatorWsEndpoint(@Qualifier(WS_ENDPOINT_ADAPTER_BEAN_NAME) SimulatorEndpointAdapter simulatorWsEndpointAdapter) {
        WebServiceEndpoint webServiceEndpoint = new WebServiceEndpoint();
        simulatorWsEndpointAdapter.setMappingKeyExtractor(simulatorWsScenarioMapper());
        simulatorWsEndpointAdapter.setFallbackEndpointAdapter(simulatorWsFallbackEndpointAdapter());

        webServiceEndpoint.setEndpointAdapter(simulatorWsEndpointAdapter);

        return webServiceEndpoint;
    }

    @Bean(name = WS_ENDPOINT_ADAPTER_BEAN_NAME)
    public SimulatorEndpointAdapter simulatorWsEndpointAdapter(ApplicationContext applicationContext, CorrelationHandlerRegistry handlerRegistry, ScenarioExecutorService scenarioExecutorService, SimulatorConfigurationProperties configuration) {
        return new SimulatorEndpointAdapter(applicationContext, handlerRegistry, scenarioExecutorService, configuration);
    }

    @Bean
    public ScenarioMapper simulatorWsScenarioMapper() {
        if (nonNull(configurer)) {
            return configurer.scenarioMapper();
        }

        return new ContentBasedXPathScenarioMapper().addXPathExpression("local-name(/*)");
    }

    @Bean
    public EndpointAdapter simulatorWsFallbackEndpointAdapter() {
        if (nonNull(configurer)) {
            return configurer.fallbackEndpointAdapter();
        }

        return new EmptyResponseEndpointAdapter();
    }

    /**
     * By registering an empty {@link WsConfigurationSupport} we make sure
     * the {@link org.springframework.boot.autoconfigure.webservices.WebServicesAutoConfiguration} will be skipped. It
     * is only conditionally enabled, based on the {@link WsConfigurationSupport} being present (disabled), or not
     * (enabled).
     *
     * @return a default web service configuration support
     * @see <a href="https://github.com/citrusframework/citrus-simulator/issues/210">Combined Sample of REST and WS</a>
     */
    @Bean
    @ConditionalOnProperty(prefix = "spring.webservices.autoconfiguration", value = "enabled", havingValue = "false", matchIfMissing = true)
    public WsConfigurationSupport wsConfigurationSupport() {
        return new WsConfigurationSupport();
    }

    @Bean
    @ConditionalOnMissingBean(WsdlScenarioGenerator.class)
    @ConditionalOnProperty(prefix = "citrus.simulator.ws.wsdl", value = "enabled", havingValue = "true")
    public WsdlScenarioGenerator simulatorWsdlScenarioGenerator() {
        return new WsdlScenarioGenerator(simulatorWebServiceConfiguration);
    }

    /**
     * Gets the web service message dispatcher servlet mapping. Clients must use this
     * context path in order to access the web service support on the simulator.
     */
    protected String[] getServletMapping() {
        if (nonNull(configurer)) {
            return configurer.servletMappings(simulatorWebServiceConfiguration).toArray(new String[0]);
        }

        return simulatorWebServiceConfiguration.getServletMappings().toArray(new String[0]);
    }

    /**
     * Provides list of endpoint interceptors.
     */
    protected EndpointInterceptor[] interceptors() {
        List<EndpointInterceptor> interceptors = new ArrayList<>();
        if (nonNull(configurer)) {
            addAll(interceptors, configurer.interceptors());
        }
        interceptors.add(loggingEndpointInterceptor);
        return interceptors.toArray(new EndpointInterceptor[0]);
    }
}
