package org.citrusframework.simulator.service.dto;

public record InteractionDto(
    String description,
    RequestAndResponseDto responseDto,
    RequestAndResponseDto requestDto
) {
}
