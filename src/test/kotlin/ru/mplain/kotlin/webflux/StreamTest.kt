package ru.mplain.kotlin.webflux

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType.APPLICATION_NDJSON
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StreamTest(@LocalServerPort port: Int) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val webClient = WebClient.create("http://localhost:$port")

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