package ru.mplain.kotlin.webflux.kafka

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("kafka")
@ConstructorBinding
data class KafkaConfig(
        val bootstrapServers: List<String> = listOf("localhost:9092"),
        val consumerClientId: String,
        val consumerGroupId: String,
        val defaultTopic: String
)