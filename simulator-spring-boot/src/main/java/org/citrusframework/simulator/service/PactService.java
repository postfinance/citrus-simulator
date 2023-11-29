package org.citrusframework.simulator.service;

import org.citrusframework.simulator.service.criteria.MessageCriteria;
import org.citrusframework.simulator.service.dto.PactDto;

import java.util.List;

public interface PactService {
    List<PactDto> getPactData(MessageCriteria messageCriteria);
}
