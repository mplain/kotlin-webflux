package ru.mplain.kotlin.webflux

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import ru.mplain.kotlin.webflux.config.KafkaConfig
import ru.mplain.kotlin.webflux.router.KafkaRouter

@SpringBootTest
@AutoConfigureWebTestClient
@EnableAutoConfiguration(exclude = [KafkaAutoConfiguration::class, MongoReactiveAutoConfiguration::class])
@MockBean(KafkaConfig::class, KafkaRouter::class, ReactiveMongoDatabaseFactory::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VideoTest(
    @Autowired val webTestClient: WebTestClient
) {

    @Test
    fun getAllVideos() {
        webTestClient
            .get()
            .uri("/api/video")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<List<String>>().isEqualTo(listOf("sample1"))
    }

    @Test
    fun getVideoById() {
        webTestClient
            .get()
            .uri("/api/video/sample1")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType("video/mp4")
    }

    @Test
    fun getVideoById_fail() {
        webTestClient
            .get()
            .uri("/api/video/sample5")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun getPartialContent() {
        webTestClient
            .get()
            .uri("/api/video/sample1")
            .header(HttpHeaders.RANGE, "bytes=0-1000")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.PARTIAL_CONTENT)
            .expectHeader().contentLength(1001)
            .expectHeader().contentType("video/mp4")
    }
}