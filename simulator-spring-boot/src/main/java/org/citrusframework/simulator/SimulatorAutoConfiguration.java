/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.simulator;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusSpringContextProvider;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.simulator.config.SimulatorConfigurationProperties;
import org.citrusframework.simulator.config.SimulatorImportSelector;
import org.citrusframework.simulator.correlation.CorrelationHandlerRegistry;
import org.citrusframework.simulator.dictionary.InboundXmlDataDictionary;
import org.citrusframework.simulator.dictionary.OutboundXmlDataDictionary;
import org.citrusframework.simulator.repository.RepositoryConfig;
import org.citrusframework.simulator.scenario.ScenarioBeanNameGenerator;
import org.citrusframework.spi.CitrusResourceWrapper;
import org.citrusframework.variable.dictionary.json.JsonPathMappingDataDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * @author Christoph Deppisch
 */
@Configuration
@EnableConfigurationProperties(SimulatorConfigurationProperties.class)
@PropertySource(value = {"META-INF/citrus-simulator.properties"}, ignoreResourceNotFound = true)
@Import(value = {CitrusSpringConfig.class, SimulatorImportSelector.class, RepositoryConfig.class})
@ImportResource(locations = {"classpath*:citrus-simulator-context.xml", "classpath*:META-INF/citrus-simulator-context.xml"})
@ComponentScan(basePackages = {"org.citrusframework.simulator.web.rest", "org.citrusframework.simulator.listener", "org.citrusframework.simulator.service", "org.citrusframework.simulator.endpoint",}, nameGenerator = ScenarioBeanNameGenerator.class)
@ConditionalOnProperty(prefix = "citrus.simulator", value = "enabled", havingValue = "true", matchIfMissing = true)
public class SimulatorAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SimulatorAutoConfiguration.class);

    private static String version;

    private final SimulatorConfigurationProperties simulatorConfiguration;

    /* Load application version */
    static {
        try (final InputStream in = new ClassPathResource("META-INF/app.version").getInputStream()) {
            Properties versionProperties = new Properties();
            versionProperties.load(in);
            version = versionProperties.get("app.version").toString();
        } catch (IOException e) {
            logger.warn("Unable to read application version information", e);
            version = "";
        }
    }

    @Autowired
    public SimulatorAutoConfiguration(SimulatorConfigurationProperties simulatorConfiguration) {
        this.simulatorConfiguration = simulatorConfiguration;
    }

    /**
     * Gets the version.
     *
     * @return
     */
    public static String getVersion() {
        return version;
    }

    @Bean
    public Citrus citrus(ApplicationContext applicationContext) {
        return Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
    }

    @Bean
    public CorrelationHandlerRegistry correlationHandlerRegistry() {
        return new CorrelationHandlerRegistry();
    }

    @Bean
    @ConditionalOnProperty(prefix = "citrus.simulator.inbound.xml.dictionary", value = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(InboundXmlDataDictionary.class)
    public InboundXmlDataDictionary inboundXmlDataDictionary() {
        var inboundXmlDataDictionary = new InboundXmlDataDictionary(simulatorConfiguration);
        inboundXmlDataDictionary.setGlobalScope(false);
        return inboundXmlDataDictionary;
    }

    @Bean
    @ConditionalOnProperty(prefix = "citrus.simulator.outbound.xml.dictionary", value = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(OutboundXmlDataDictionary.class)
    public OutboundXmlDataDictionary outboundXmlDataDictionary() {
        var outboundXmlDataDictionary = new OutboundXmlDataDictionary(simulatorConfiguration);
        outboundXmlDataDictionary.setGlobalScope(false);
        return outboundXmlDataDictionary;
    }

    @Bean
    @ConditionalOnMissingBean(name = "inboundJsonDataDictionary")
    @ConditionalOnProperty(prefix = "citrus.simulator.inbound.json.dictionary", value = "enabled", havingValue = "true")
    public JsonPathMappingDataDictionary inboundJsonDataDictionary() {
        var inboundJsonDataDictionary = new JsonPathMappingDataDictionary();
        inboundJsonDataDictionary.setMappings(new LinkedHashMap<>());
        inboundJsonDataDictionary.setGlobalScope(false);

        Resource mappingFile = new PathMatchingResourcePatternResolver().getResource(simulatorConfiguration.getInboundJsonDictionary());
        if (mappingFile.exists()) {
            inboundJsonDataDictionary.setMappingFile(new CitrusResourceWrapper(mappingFile));
        }

        return inboundJsonDataDictionary;
    }

    @Bean
    @ConditionalOnMissingBean(name = "outboundJsonDataDictionary")
    @ConditionalOnProperty(prefix = "citrus.simulator.outbound.json.dictionary", value = "enabled", havingValue = "true")
    public JsonPathMappingDataDictionary outboundJsonDataDictionary() {
        var outboundJsonDataDictionary = new JsonPathMappingDataDictionary();
        outboundJsonDataDictionary.setMappings(new LinkedHashMap<>());
        outboundJsonDataDictionary.setGlobalScope(false);

        Resource mappingFile = new PathMatchingResourcePatternResolver().getResource(simulatorConfiguration.getOutboundJsonDictionary());
        if (mappingFile.exists()) {
            outboundJsonDataDictionary.setMappingFile(new CitrusResourceWrapper(mappingFile));
        }

        return outboundJsonDataDictionary;
    }
}
