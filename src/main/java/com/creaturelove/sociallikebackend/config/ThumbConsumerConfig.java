package com.creaturelove.sociallikebackend.config;

import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.RedeliveryBackoff;
import org.apache.pulsar.client.impl.MultiplierRedeliveryBackoff;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.annotation.PulsarListenerConsumerBuilderCustomizer;

import java.util.concurrent.TimeUnit;
@Configuration
public class ThumbConsumerConfig <T> implements PulsarListenerConsumerBuilderCustomizer<T> {
    @Override
    public void customize(ConsumerBuilder<T> consumerBuilder) {
        consumerBuilder.batchReceivePolicy(
                BatchReceivePolicy.builder()
                        // 1000 a time
                        .maxNumMessages(1000)
                        // TTL
                        .timeout(10000, TimeUnit.MILLISECONDS)
                        .build()
        );
    }

    // Configure ACK TimeOut Retry Policy
    @Bean
    public RedeliveryBackoff ackTimeoutRedeliveryBackoff() {
        return MultiplierRedeliveryBackoff.builder()
                // initial delay 5 s
                .minDelayMs(5000)
                // Max delay 300 s
                .maxDelayMs(300_000)
                .multiplier(3)
                .build();
    }

    @Bean
    public DeadLetterPolicy deadLetterPolicy() {
        return DeadLetterPolicy.builder()
                // max retry count 3
                .maxRedeliverCount(3)
                // dead letter topic
                .deadLetterTopic("thumb-dlq-topic")
                .build();
    }
}
