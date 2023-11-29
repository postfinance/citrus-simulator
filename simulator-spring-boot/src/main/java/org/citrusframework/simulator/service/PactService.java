package org.citrusframework.simulator.service;

import au.com.dius.pact.core.model.RequestResponsePact;
import org.citrusframework.simulator.service.criteria.MessageCriteria;
import org.citrusframework.simulator.service.dto.PactDto;

import java.util.List;

public interface PactService {
    List<RequestResponsePact> getPactData(MessageCriteria messageCriteria);
}
