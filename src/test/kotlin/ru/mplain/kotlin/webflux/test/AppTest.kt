package ru.mplain.kotlin.webflux.test

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.test.web.reactive.server.returnResult
import ru.mplain.kotlin.webflux.*

class AppTest : AbstractTest() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Test
    fun post_missing_time_bad_request() {
        post(createEvent(time = null))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder(EVENT_TIME, "is a non-nullable type"))
    }

    @Test
    fun post_missing_type_bad_request() {
        post(createEvent(type = null))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder(EVENT_TYPE, "is a non-nullable type"))
    }

    @Test
    fun post_missing_data_bad_request() {
        post(createEvent(data = null))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder(EVENT_DATA, "is a non-nullable type"))
    }

    @Test
    fun post_invalid_type_bad_request() {
        val invalidType = "typeNA"
        post(createEvent(type = invalidType))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .value(Matchers.stringContainsInOrder("Cannot deserialize value", "from String \"$invalidType\""))
    }

    @Test
    fun post_extra_field_ok_not_stored() {
        val extraKey = "id"
        post(createEvent(extra = extraKey to "12345"))
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.$extraKey").doesNotExist()
                .jsonPath("$.$EVENT_TIME").exists()
                .jsonPath("$.$EVENT_TYPE").exists()
                .jsonPath("$.$EVENT_DATA").exists()
    }

    @Test
    fun get_no_params_ok(): Unit = runBlocking {
        get(emptyMap())
                .expectStatus().isOk
                .returnResult<String>()
                .responseBody
                .awaitSingle()
                .let(logger::info)
    }

    @Test
    fun get_filter_ok(): Unit = runBlocking {
        get(mapOf(EVENT_TYPE to randomType))
                .expectStatus().isOk
                .returnResult<String>()
                .responseBody
                .awaitSingle()
                .let(logger::info)
    }

    @Test
    fun get_filter_bad_request() {
        val invalidType = "typeNA"
        get(mapOf(QUERY_TYPE to invalidType))
                .expectStatus().isBadRequest
                .expectBody().jsonPath("$.message")
                .isEqualTo("Invalid event type requested: $invalidType")
    }

    @Test
    fun get_pagination_ok(): Unit = runBlocking {
        get(mapOf(QUERY_PAGE to 2, QUERY_SIZE to 10))
                .expectStatus().isOk
                .returnResult<String>()
                .responseBody
                .awaitSingle()
                .let(logger::info)
    }

    @Test
    fun get_pagination_not_found() {
        get(mapOf(QUERY_PAGE to 5, QUERY_SIZE to 100))
                .expectStatus().isNotFound
                .expectBody().isEmpty
    }
}