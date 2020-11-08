package ru.mplain.kotlin.webflux.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ClassUtils
import org.springframework.web.reactive.function.server.*
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord

@Configuration
class KafkaRouter(
        private val kafkaConfig: KafkaConfig,
        private val kafkaSender: KafkaSender<String, Any>,
        private val kafkaReceiver: KafkaReceiver<String, Any>
) {
    private val logger = LoggerFactory.getLogger(ClassUtils.getUserClass(javaClass))

    @Bean
    fun kafkaRoutes() = router {
        POST("/kafka") { request ->
            val record = request.bodyToFlux<Any>()
                    .doOnNext { logger.info("Event received by server: $it") }
                    .map { ProducerRecord<String, Any>(kafkaConfig.defaultTopic, it) }
                    .map { SenderRecord.create(it, System.currentTimeMillis()) }
            val result = kafkaSender.send(record)
                    .doOnNext { logger.info("Event sent to Kafka: $it") }
                    .map { it.correlationMetadata() }
            ServerResponse.ok().body(result)
        }

        GET("/kafka") {
            val flux = kafkaReceiver.receive()
                    .doOnNext { logger.info("Event received from Kafka: $it") }
                    .doOnNext { it.receiverOffset().acknowledge() }
                    .map { it.value() }
                    .doOnNext { logger.info("Event sent to client: $it") }
            ServerResponse.ok().sse().body(flux)
        }
    }
}