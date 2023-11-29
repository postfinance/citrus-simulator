package org.citrusframework.simulator.service.dto;


import java.util.List;
import java.util.Map;

public record PactDto(Map<String, String> consumer, Map<String, String> provider, List<InteractionDto> interactions,
                      Map<String, Map<String, String>> metadata) {
}
