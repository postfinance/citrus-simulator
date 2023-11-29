package org.citrusframework.simulator.web.rest;

import org.citrusframework.simulator.service.criteria.MessageCriteria;
import org.citrusframework.simulator.service.dto.PactDto;
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
    public ResponseEntity<PactDto> getPactData(MessageCriteria criteria) {
        return ResponseEntity.ok(pactService.getPactData(criteria));
    }
}
