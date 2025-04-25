package com.creaturelove.sociallikebackend.config;

import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.springframework.pulsar.annotation.PulsarListenerConsumerBuilderCustomizer;

import java.util.concurrent.TimeUnit;

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
}
