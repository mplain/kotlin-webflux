package ru.mplain.spring.boot.test

import org.hamcrest.Matchers
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.CollectionUtils
import org.springframework.web.util.UriBuilder
import ru.mplain.spring.boot.domain.Event.Companion.EVENT_DATA
import ru.mplain.spring.boot.domain.Event.Companion.EVENT_TIME
import ru.mplain.spring.boot.domain.Event.Companion.EVENT_TYPE
import ru.mplain.spring.boot.reactive.Handler.Companion.QUERY_PAGE
import ru.mplain.spring.boot.reactive.Handler.Companion.QUERY_SIZE
import ru.mplain.spring.boot.reactive.Handler.Companion.QUERY_TYPE
import ru.mplain.spring.boot.test.TestUtils.createEvent
import ru.mplain.spring.boot.test.TestUtils.createRandomEvent
import ru.mplain.spring.boot.test.TestUtils.randomDateTime
import ru.mplain.spring.boot.test.TestUtils.randomType
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestMethodOrder(OrderAnnotation::class)
@AutoConfigureWebTestClient(timeout = "60000")
class AppTest() {
    companion object {
        const val INVALID_TYPE = "typeNA"
    }

    val logger: Logger = LoggerFactory.getLogger(AppTest::class.java)

    @Autowired
    lateinit var webClient: WebTestClient

    fun post(event: String): WebTestClient.ResponseSpec {
        return webClient
                .post()
                .uri("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(event)
                .exchange()
    }

    fun get(map: Map<String, Any>): WebTestClient.ResponseSpec {
        val queryParams = CollectionUtils.toMultiValueMap(map.mapValues { listOf(it.toString()) })
        return webClient
                .get()
                .uri { b: UriBuilder -> b.path("/events").queryParams(queryParams).build() }
                .exchange()
    }

    @Test
    @Order(-2)
    fun populate_database() {
        for (i in 1..20) post(createRandomEvent())
    }

    @Test
    fun post_missing_time_bad_request() {
        post(createEvent(null, randomType(), UUID.randomUUID().toString()))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder(EVENT_TIME, "is a non-nullable type"))

    }

    @Test
    fun post_missing_type_bad_request() {
        post(createEvent(randomDateTime(), null, UUID.randomUUID().toString()))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder(EVENT_TYPE, "is a non-nullable type"))
    }

    @Test
    fun post_missing_data_bad_request() {
        post(createEvent(randomDateTime(), randomType(), null))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder(EVENT_DATA, "is a non-nullable type"))
    }

    @Test
    fun post_invalid_type_bad_request() {
        post(createEvent(randomDateTime(), INVALID_TYPE, UUID.randomUUID().toString()))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder("Cannot deserialize value", "from String \"typeNA\""))
    }

    @Test
    fun post_extra_field_ok_not_stored() {
        val extraKey = "id"
        post(createEvent(randomDateTime(), randomType(), UUID.randomUUID().toString(), extraKey, "12345"))
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.$extraKey").doesNotExist()
                .jsonPath("$.$EVENT_TIME").exists()
                .jsonPath("$.$EVENT_TYPE").exists()
                .jsonPath("$.$EVENT_DATA").exists()
    }

    @Test
    fun get_no_params_ok() {
        get(emptyMap())
                .expectStatus().isOk
                .returnResult(String::class.java)
                .responseBody
                .subscribe { logger.info(it) }
    }

    @Test
    fun get_filter_ok() {
        get(java.util.Map.of<String, Any>(EVENT_TYPE, randomType()))
                .expectStatus().isOk
                .returnResult(String::class.java)
                .responseBody
                .subscribe { logger.info(it) }
    }

    @Test
    fun get_filter_bad_request() {
        get(java.util.Map.of<String, Any>(QUERY_TYPE, INVALID_TYPE))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .isEqualTo("Invalid event type requested: $INVALID_TYPE")
    }

    @Test
    fun get_pagination_ok() {
        get(java.util.Map.of<String, Any>(QUERY_PAGE, 2, QUERY_SIZE, 10))
                .expectStatus().isOk
                .returnResult(String::class.java)
                .responseBody
                .subscribe { logger.info(it) }
    }

    @Test
    fun get_pagination_bad_request() {
        get(java.util.Map.of<String, Any>("page", 5, "size", 100))
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("No events retrieved by request!")
    }
}