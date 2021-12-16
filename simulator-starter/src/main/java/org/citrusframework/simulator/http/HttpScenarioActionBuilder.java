package org.citrusframework.simulator.http;

import com.consol.citrus.dsl.builder.HttpActionBuilder;
import com.consol.citrus.spi.ReferenceResolver;
import org.citrusframework.simulator.scenario.ScenarioEndpoint;

/**
 * @author Christoph Deppisch
 */
public class HttpScenarioActionBuilder extends HttpActionBuilder {

    /** Scenario endpoint */
    private final ScenarioEndpoint scenarioEndpoint;
    
    public HttpScenarioActionBuilder(ScenarioEndpoint scenarioEndpoint) {
        this.scenarioEndpoint = scenarioEndpoint;
    }

    /**
     * Sets the bean reference resolver.
     * @param referenceResolver
     */
    public HttpScenarioActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        super.withReferenceResolver(referenceResolver);
        return this;
    }

    /**
     * Default scenario receive operation.
     * @return
     */
    public HttpServerReceiveActionBuilder receive() {
        HttpServerReceiveActionBuilder receiveActionBuilder = server(scenarioEndpoint).receive();
        return receiveActionBuilder;
    }

    /**
     * Default scenario send response operation.
     * @return
     */
    public HttpServerSendActionBuilder send() {
        HttpServerSendActionBuilder sendActionBuilder = server(scenarioEndpoint).send();
        return sendActionBuilder;
    }
    
    public HttpServerActionBuilder server(ScenarioEndpoint scenarioEndpoint) {
        return server(scenarioEndpoint);
    }
}
