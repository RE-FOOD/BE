package com.iitp.domains.order.event;

import com.iitp.domains.order.dto.OrderEventDto;
import com.iitp.domains.order.service.command.OrderCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {
    private final OrderCommandService  orderCommandService;

    @KafkaListener(topics = "order-issue_requests", groupId = "order-service", containerFactory = "orderKafkaListenerContainerFactory")
    public void consumerOrderIssueRequest(OrderEventDto.IssueMessage message) {
        try{
            log.info("Received Order issue reuqest: {}", message);
            orderCommandService.issueOrder(message);
        }catch (Exception e){
            log.error("Failed to process Order issue reuqest: {}", e.getMessage(), e);
        }
    }
}
