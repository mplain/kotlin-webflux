package ru.mplain.kotlin.webflux.kafka

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import reactor.core.publisher.Flux
import ru.mplain.kotlin.webflux.common.Event
import ru.mplain.kotlin.webflux.createEvent
import ru.mplain.kotlin.webflux.mongodb.MongoHandler
import ru.mplain.kotlin.webflux.mongodb.MongoRouter
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration(exclude = [MongoReactiveAutoConfiguration::class])
@MockBean(MongoRouter::class, MongoHandler::class)
class KafkaTest {
    val logger = LoggerFactory.getLogger(javaClass)
    val webClient = WebClient.create("http://localhost:8080")

    companion object {
        val container = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))

        @JvmStatic
        @DynamicPropertySource
        fun setup(registry: DynamicPropertyRegistry) {
            container.start()
            registry.add("spring.kafka.bootstrap-servers", container::getBootstrapServers)
        }
    }

    @Test
    fun test() {
        webClient
                .get()
                .uri("/kafka")
                .retrieve()
                .bodyToFlux<Event>()
                .subscribe { logger.info("Subscriber received: $it\n") }

        val flux = Flux.interval(Duration.ofSeconds(1))
                .map { createEvent() }
                .doOnNext { logger.info("Producer sent: $it") }
                .take(5)

        webClient
                .post()
                .uri("/kafka")
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(flux)
                .retrieve()
                .bodyToFlux<Long>()
                .doOnNext { logger.info("Producer received: $it") }
                .blockLast()
    }
}