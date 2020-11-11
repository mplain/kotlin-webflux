package ru.mplain.kotlin.webflux.mongodb

import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.testcontainers.containers.MongoDBContainer
import ru.mplain.kotlin.webflux.common.*
import ru.mplain.kotlin.webflux.createEvent
import ru.mplain.kotlin.webflux.get
import ru.mplain.kotlin.webflux.kafka.KafkaConfig
import ru.mplain.kotlin.webflux.kafka.KafkaRouter
import ru.mplain.kotlin.webflux.post
import ru.mplain.kotlin.webflux.randomType

@SpringBootTest
@AutoConfigureWebTestClient
@EnableAutoConfiguration(exclude = [KafkaAutoConfiguration::class])
@MockBean(KafkaConfig::class, KafkaRouter::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoTest(
        @Autowired val webTestClient: WebTestClient
) {
    val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        val container = MongoDBContainer("mongo:4.0.10")

        @JvmStatic
        @DynamicPropertySource
        fun setup(registry: DynamicPropertyRegistry) {
            container.start()
            registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl)
        }
    }

    @BeforeAll
    fun setup() {
        for (i in 1..20) webTestClient.post(createEvent())
    }

    @Test
    fun post_missing_time_bad_request() {
        webTestClient
                .post(createEvent(time = null))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder(EVENT_TIME, "is a non-nullable type"))
    }

    @Test
    fun post_missing_type_bad_request() {
        webTestClient
                .post(createEvent(type = null))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder(EVENT_TYPE, "is a non-nullable type"))
    }

    @Test
    fun post_missing_data_bad_request() {
        webTestClient
                .post(createEvent(data = null))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder(EVENT_DATA, "is a non-nullable type"))
    }

    @Test
    fun post_invalid_type_bad_request() {
        val invalidType = "typeNA"
        webTestClient
                .post(createEvent(type = invalidType))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder("Cannot deserialize value", "from String \"$invalidType\""))
    }

    @Test
    fun post_extra_field_ok_not_stored() {
        val extraKey = "id"
        webTestClient
                .post(createEvent(extraKey to "12345"))
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.$extraKey").doesNotExist()
                .jsonPath("$.$EVENT_TIME").exists()
                .jsonPath("$.$EVENT_TYPE").exists()
                .jsonPath("$.$EVENT_DATA").exists()
    }

    @Test
    fun get_no_params_ok() {
        webTestClient
                .get(emptyMap())
                .expectStatus().isOk
                .expectBody<String>()
                .returnResult()
                .responseBody
                .also(logger::info)
    }

    @Test
    fun get_filter_ok() {
        webTestClient
                .get(mapOf(EVENT_TYPE to randomType))
                .expectStatus().isOk
                .expectBody<String>()
                .returnResult()
                .responseBody
                .also(logger::info)
    }

    @Test
    fun get_filter_bad_request() {
        val invalidType = "typeNA"
        webTestClient
                .get(mapOf(QUERY_TYPE to invalidType))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .isEqualTo("Invalid event type requested: $invalidType")
    }

    @Test
    fun get_pagination_ok() {
        webTestClient
                .get(mapOf(QUERY_PAGE to 2, QUERY_SIZE to 10))
                .expectStatus().isOk
                .expectBody<String>()
                .returnResult()
                .responseBody
                .also(logger::info)
    }

    @Test
    fun get_pagination_not_found() {
        webTestClient
                .get(mapOf(QUERY_PAGE to 5, QUERY_SIZE to 100))
                .expectStatus().isNotFound
                .expectBody().isEmpty
    }
}