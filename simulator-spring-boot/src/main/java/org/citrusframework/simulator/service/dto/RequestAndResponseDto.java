package org.citrusframework.simulator.service.dto;

import org.citrusframework.simulator.model.Message;
import org.citrusframework.simulator.model.MessageHeader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public class RequestAndResponseDto {
    public String method;
    public String path;
    public Map<String, List<String>> header;
    public Map<String, List<String>> queryParameters;
    public Map<String, List<String>> cookieDto;
    public String body;


    public RequestAndResponseDto(Message message) {
        this.body = message.getPayload();
        Map<String, List<String>> messageHeaders = message.getHeaders().stream().collect(Collectors.groupingBy(MessageHeader::getName, Collectors.mapping(MessageHeader::getValue, Collectors.toList())));
        if (!messageHeaders.isEmpty()) {
            this.method = returnValueIfExisting("citrus_http_method", messageHeaders);
            this.path = returnValueIfExisting("citrus_http_request_uri", messageHeaders);
            this.header = messageHeaders.entrySet().stream().filter(entry -> entry.getKey().startsWith("citrus_")).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }
    }

    private String returnValueIfExisting(String key, Map<String, List<String>> messageHeaders) {
        if (!messageHeaders.get(key).isEmpty()) {
            return messageHeaders.get(key).get(0);
        }
        return null;
    }
}
