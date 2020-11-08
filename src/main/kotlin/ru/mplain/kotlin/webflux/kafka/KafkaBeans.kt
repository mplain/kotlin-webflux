package ru.mplain.kotlin.webflux.kafka

import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaBeans(
        private val kafkaConfig: KafkaConfig
) {

    @Bean
    fun kafkaSender(): KafkaSender<String, Any> {
        val senderConfig = mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        val senderOptions = SenderOptions.create<String, Any>(senderConfig)
        return KafkaSender.create(senderOptions)
    }

    @Bean
    fun kafkaReceiver(): KafkaReceiver<String, Any> {
        val receiverConfig = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                ConsumerConfig.GROUP_ID_CONFIG to kafkaConfig.consumerClientId,
                ConsumerConfig.CLIENT_ID_CONFIG to kafkaConfig.consumerGroupId
        )
        val receiverOptions = ReceiverOptions.create<String, Any>(receiverConfig)
                .subscription(setOf(kafkaConfig.defaultTopic))
        return KafkaReceiver.create(receiverOptions)
    }
}