package ru.mplain.kotlin.webflux.stream

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_NDJSON
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux
import ru.mplain.kotlin.webflux.kafka.KafkaConfig
import ru.mplain.kotlin.webflux.kafka.KafkaRouter
import ru.mplain.kotlin.webflux.mongodb.MongoHandler
import ru.mplain.kotlin.webflux.mongodb.MongoRouter
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration(exclude = [MongoReactiveAutoConfiguration::class, KafkaAutoConfiguration::class])
@MockBean(KafkaConfig::class, KafkaRouter::class, MongoRouter::class, MongoHandler::class)
class StreamTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val webClient = WebClient.create("http://localhost:8080")

    @Test
    fun test() {
        webClient
                .get()
                .uri("/stream")
                .retrieve()
                .bodyToFlux<String>()
                .subscribe { logger.info("Client received: $it") }

        val flux = Flux.interval(Duration.ofSeconds(1))
                .doOnNext { logger.info("Client sent: $it") }
                .take(5)

        webClient
                .post()
                .uri("/stream")
                .contentType(APPLICATION_NDJSON)
                .body(flux)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess { logger.info("Response received: $it") }
                .block()
    }
}