package com.iitp.global.config;

import com.iitp.domains.order.dto.OrderEventDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID = "order-service";
    private static final String TOPIC = "order-topic";

    @Bean
    public ProducerFactory<String, OrderEventDto.IssueMessage> orderProducerFactory(){
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);     // 키 직렬화
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);     // Value 값을 JSON형태로 넣을때 JsonSerializer추가
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS,true);  // 헤더에 타입 정보 추가


        // 안정성을 위한 추가 설정
        config.put(ProducerConfig.ACKS_CONFIG, "all");      // 모든 복제본에 쓰기 완료 확인
        config.put(ProducerConfig.RETRIES_CONFIG, 3);       // 재시도 횟수
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);        // 순서 보장
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, OrderEventDto.IssueMessage> orderKafkaTemplate(){
        return new KafkaTemplate<>(orderProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, OrderEventDto.IssueMessage> orderConsumerTemplate(){
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);       // 컨슈머 그룹
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // 안전성을 위한 추가 설정
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");        // 처음부터 읽기
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);            // 수동 커밋
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);            // 한 번에 최대 100개 처리

        JsonDeserializer<OrderEventDto.IssueMessage> jsonDeserializer = new JsonDeserializer<>(OrderEventDto.IssueMessage.class);
        jsonDeserializer.addTrustedPackages("*");       // 모든 패키지 신뢰
        jsonDeserializer.setUseTypeHeaders(true);   // 타입 매핑 활성화
        jsonDeserializer.setRemoveTypeHeaders(false);   // 헤더 유지

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                jsonDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEventDto.IssueMessage> couponKafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, OrderEventDto.IssueMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderConsumerTemplate());
        // 동시성 설정
        factory.setConcurrency(3);

        return factory;
    }

}
