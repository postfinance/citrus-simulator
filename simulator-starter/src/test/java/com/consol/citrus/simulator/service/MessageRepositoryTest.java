package com.consol.citrus.simulator.service;

import com.consol.citrus.simulator.SimulatorAutoConfiguration;
import com.consol.citrus.simulator.config.SimulatorConfigurationProperties;
import com.consol.citrus.simulator.model.Message;
import com.consol.citrus.simulator.model.Message.Direction;
import com.consol.citrus.simulator.model.MessageFilter;
import com.consol.citrus.simulator.repository.MessageRepository;
import org.testng.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = { SimulatorAutoConfiguration.class })
class MessageRepositoryTest {

    private static final QueryFilterAdapterFactory QUERY_FILTER_ADAPTER_FACTORY = new QueryFilterAdapterFactory(
            new SimulatorConfigurationProperties());

    private static final String PAYLOAD = "This is a test message!";
    private static final String HEADER_NAME = "h1";
    private static final String HEADER_NAME2 = "h2";

    @Autowired
    private MessageService service;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void testFindByHeader() {
        String uid = createTestMessage();

        MessageFilter filter = new MessageFilter();
        filter.setDirectionOutbound(false);
        filter.setHeaderFilter(HEADER_NAME + ":" + uid);

        MessageFilter filterAdapter = QUERY_FILTER_ADAPTER_FACTORY.getQueryAdapter(filter);

        List<Message> result = messageRepository.find(filterAdapter);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(uid, result.get(0).getCitrusMessageId());
        Assert.assertEquals(PAYLOAD, result.get(0).getPayload());

        filter.setHeaderFilter(HEADER_NAME + ":" + uid + "_3");
        result = messageRepository.find(filterAdapter);
        Assert.assertEquals(0, result.size());
    }

    @Test
    void testFindByHeaderMulti() {
        String uid = createTestMessage();

        MessageFilter filter = new MessageFilter();
        filter.setDirectionOutbound(false);
        filter.setHeaderFilter(HEADER_NAME + ":" + uid + ";" + HEADER_NAME2 + ":" + uid + "_2");

        MessageFilter filterAdapter = QUERY_FILTER_ADAPTER_FACTORY.getQueryAdapter(filter);

        List<Message> result = messageRepository.find(filterAdapter);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(uid, result.get(0).getCitrusMessageId());
        Assert.assertEquals(PAYLOAD, result.get(0).getPayload());

        filter.setHeaderFilter(HEADER_NAME + ":" + uid + ";" + HEADER_NAME2 + ":" + uid + "_3");

        result = messageRepository.find(filterAdapter);
        Assert.assertEquals(0, result.size());
    }

    @Test
    void testFindByHeaderLike() {
        String innerUid = UUID.randomUUID().toString();
        String uid = createTestMessage("PRE" + innerUid + "POST", PAYLOAD);

        MessageFilter filter = new MessageFilter();
        filter.setDirectionOutbound(false);

        filter.setHeaderFilter(HEADER_NAME + ":" + "%" + innerUid + "%");

        MessageFilter filterAdapter = QUERY_FILTER_ADAPTER_FACTORY.getQueryAdapter(filter);
        List<Message> result = messageRepository.find(filterAdapter);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(uid, result.get(0).getCitrusMessageId());
        Assert.assertEquals(PAYLOAD, result.get(0).getPayload());
    }

    @Test
    void testFindByPayloadLike() {
        String uid = UUID.randomUUID().toString();
        String specificPayload = "Pay" + uid + "LOAD";
        uid = createTestMessage(uid, specificPayload);

        MessageFilter filter = new MessageFilter();
        filter.setDirectionOutbound(false);
        filter.setContainingText("%" + uid + "%");

        MessageFilter filterAdapter = QUERY_FILTER_ADAPTER_FACTORY.getQueryAdapter(filter);
        List<Message> result = messageRepository.find(filterAdapter);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(uid, result.get(0).getCitrusMessageId());
        Assert.assertEquals(specificPayload, result.get(0).getPayload());
    }

    @Test
    void testFindByPayload() {
        String uid = createTestMessage();

        MessageFilter filter = new MessageFilter();
        filter.setDirectionOutbound(false);
        filter.setContainingText(PAYLOAD);

        MessageFilter filterAdapter = QUERY_FILTER_ADAPTER_FACTORY.getQueryAdapter(filter);

        List<Message> result = messageRepository.find(filterAdapter);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(uid, result.get(0).getCitrusMessageId());
        Assert.assertEquals(PAYLOAD, result.get(0).getPayload());
    }

    @Test
    void testFindByAllParams() {
        Date startSavingDate = now();
        String uid = createTestMessage();
        Date endSavingDate = now();

        // Filter by all valid
        MessageFilter filter = new MessageFilter();
        filter.setFromDate(startSavingDate);
        filter.setToDate(endSavingDate);
        filter.setDirectionOutbound(false);
        filter.setContainingText(PAYLOAD);
        filter.setHeaderFilter(HEADER_NAME + ":" + uid);

        MessageFilter filterAdapter = QUERY_FILTER_ADAPTER_FACTORY.getQueryAdapter(filter);
        List<Message> result = messageRepository.find(filterAdapter);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(PAYLOAD, result.get(0).getPayload());
    }

    @Test
    void testPaging() {
        String uniquePayload = "PagingPayload" + UUID.randomUUID().toString();
        for (int i = 0; i < 100; i++) {
            createTestMessage(UUID.randomUUID().toString(), uniquePayload);
        }

        MessageFilter filter = new MessageFilter();
        filter.setDirectionOutbound(false);
        filter.setContainingText(uniquePayload);
        filter.setPageNumber(0);
        filter.setPageSize(33);

        MessageFilter filterAdapter = QUERY_FILTER_ADAPTER_FACTORY.getQueryAdapter(filter);
        List<Message> result = messageRepository.find(filterAdapter);

        Assert.assertEquals(33, result.size());
    }

    private Date now() {
        return Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    private String createTestMessage() {
        return createTestMessage(UUID.randomUUID().toString(), PAYLOAD);
    }

    private String createTestMessage(String uid, String payload) {

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(HEADER_NAME, uid);
        headers.put(HEADER_NAME2, uid + "_2");

        service.saveMessage(Direction.INBOUND, payload, uid, headers);

        return uid;
    }
}
