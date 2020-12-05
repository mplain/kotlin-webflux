package ru.mplain.kotlin.webflux.router

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ClassUtils
import org.springframework.web.reactive.function.server.*
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import ru.mplain.kotlin.webflux.model.Event

@Configuration
class KafkaRouter(
    private val kafkaProperties: KafkaProperties,
    private val kafkaSender: KafkaSender<String, Event>,
    private val kafkaReceiver: KafkaReceiver<String, Event>
) {
    private val logger = LoggerFactory.getLogger(ClassUtils.getUserClass(javaClass))

    @Bean
    fun kafkaRoutes() = router {
        POST("/kafka") { request ->
            val records = request.bodyToFlux<Event>()
                .doOnNext { logger.info("Server received: $it") }
                .map { ProducerRecord<String, Event>(kafkaProperties.template.defaultTopic, it) }
                .map { SenderRecord.create(it, System.currentTimeMillis()) }
            val result = kafkaSender.send(records)
                .doOnNext { logger.info("Server sent to Kafka: $it") }
                .map { it.correlationMetadata() }
            ServerResponse.ok().body(result)
        }

        GET("/kafka") {
            val flux = kafkaReceiver.receive()
                .doOnNext { logger.info("Server received from Kafka: $it") }
                .doOnNext { it.receiverOffset().acknowledge() }
                .map { it.value() }
                .doOnNext { logger.info("Server sent to client: $it") }
            ServerResponse.ok().sse().body(flux)
        }
    }
}