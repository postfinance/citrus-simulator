package org.citrusframework.simulator.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.citrusframework.simulator.model.Message;
import org.springframework.data.domain.Pageable;

public interface MessageRepositoryCustom {
    List<Message> findByDateBetweenAndDirectionInAndPayloadContainingIgnoreCase(Date fromDate,
                                                                                Date toDate, Collection<Message.Direction> directions, String containingText,
                                                                                String headerParameterName, String headerParameterValue, Pageable pageable);
}
