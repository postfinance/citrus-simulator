package org.citrusframework.simulator.web.rest;

import au.com.dius.pact.core.model.RequestResponsePact;
import java.util.List;
import org.citrusframework.simulator.service.criteria.MessageCriteria;
import org.citrusframework.simulator.service.impl.PactServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pact")
public class PactResource {

 private final PactServiceImpl pactService;

    public PactResource(PactServiceImpl pactService) {
        this.pactService = pactService;
    }

    @GetMapping("/data")
    public ResponseEntity<List<RequestResponsePact>> getPactData(MessageCriteria criteria) {
        return ResponseEntity.ok(pactService.getPactData(criteria));
    }
}
