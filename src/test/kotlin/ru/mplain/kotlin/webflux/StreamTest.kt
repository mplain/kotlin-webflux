package ru.mplain.kotlin.webflux

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.http.MediaType.APPLICATION_NDJSON
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux
import ru.mplain.kotlin.webflux.config.KafkaConfig
import ru.mplain.kotlin.webflux.router.KafkaRouter
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration(exclude = [KafkaAutoConfiguration::class, MongoReactiveAutoConfiguration::class])
@MockBean(KafkaConfig::class, KafkaRouter::class, ReactiveMongoDatabaseFactory::class)
class StreamTest {
    val logger = LoggerFactory.getLogger(javaClass)
    val webClient = WebClient.create("http://localhost:8080")

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