package ru.mplain.kotlin.webflux.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import ru.mplain.kotlin.webflux.common.KAFKA
import ru.mplain.kotlin.webflux.model.Event

@Configuration
@Profile(KAFKA)
class KafkaConfig(
    private val kafkaProperties: KafkaProperties
) {
    @Bean
    fun newTopic(): NewTopic = TopicBuilder.name(kafkaProperties.template.defaultTopic).build()

    @Bean
    fun kafkaSender(): KafkaSender<String, Event> {
        val bootstrapServers = kafkaProperties.producer.bootstrapServers ?: kafkaProperties.bootstrapServers
        require(bootstrapServers.isNotEmpty())
        val clientId = kafkaProperties.producer.clientId ?: kafkaProperties.clientId ?: javaClass.packageName

        val senderConfig = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.CLIENT_ID_CONFIG to clientId,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        val senderOptions = SenderOptions.create<String, Event>(senderConfig)
        return KafkaSender.create(senderOptions)
    }

    @Bean
    fun kafkaReceiver(): KafkaReceiver<String, Event> {
        val bootstrapServers = kafkaProperties.consumer.bootstrapServers ?: kafkaProperties.bootstrapServers
        val groupId = kafkaProperties.consumer.groupId
        val topic = kafkaProperties.template.defaultTopic
        require(bootstrapServers.isNotEmpty())
        require(groupId.isNotEmpty())
        require(topic.isNotEmpty())

        val receiverConfig = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java
        )
        val receiverOptions = ReceiverOptions.create<String, Event>(receiverConfig)
            .withValueDeserializer(JsonDeserializer(Event::class.java))
            .subscription(setOf(topic))
        return KafkaReceiver.create(receiverOptions)
    }
}