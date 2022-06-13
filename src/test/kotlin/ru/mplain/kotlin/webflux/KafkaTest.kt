package ru.mplain.kotlin.webflux

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import reactor.core.publisher.Flux
import ru.mplain.kotlin.webflux.common.KAFKA
import ru.mplain.kotlin.webflux.model.Event
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(KAFKA)
class KafkaTest(@LocalServerPort port: Int) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val webClient = WebClient.create("http://localhost:$port")

    companion object {
        private val container = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))

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