package com.iitp.domains.order.event;

import com.iitp.domains.order.dto.OrderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {
    private static final String TOPIC = "order-issue-requests";
    private final KafkaTemplate<String, OrderEventDto.IssueMessage> kafkaTemplate;

    public void sendOrderIssueRequest(OrderEventDto.IssueMessage message){
        kafkaTemplate.send(TOPIC, String.valueOf(message.getMemberId()), message)
                .whenComplete((result, ex) -> {
                    if(ex == null){
                        log.info("Sent message=[{}] with offset=[{}]", message, result.getRecordMetadata().offset());
                    }else{
                        log.error("Unable to send message=[{}]", message, ex.getMessage());
                    }
                });
    }
}
