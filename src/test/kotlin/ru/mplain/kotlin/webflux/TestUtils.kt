package ru.mplain.kotlin.webflux

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.CollectionUtils
import ru.mplain.kotlin.webflux.common.EVENT_DATA
import ru.mplain.kotlin.webflux.common.EVENT_TIME
import ru.mplain.kotlin.webflux.common.EVENT_TYPE
import ru.mplain.kotlin.webflux.common.Event
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

val jackson = jacksonObjectMapper()

fun WebTestClient.post(body: Any) = this
        .post()
        .uri("/event")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()

fun WebTestClient.get(params: Map<String, Any>) = this
        .get()
        .uri { it.path("/event").queryParams(params.toMultiValueMap()).build() }
        .exchange()

fun createEvent(
        vararg extra: Pair<String, String>,
        time: LocalDateTime? = randomDateTime,
        type: String? = randomType,
        data: String? = randomUUID,
) = mapOf(
        EVENT_TIME to time?.toString(),
        EVENT_TYPE to type,
        EVENT_DATA to data,
        *extra
).toJson()

fun Any.toJson(): String = jackson.writeValueAsString(this)
fun <K, V> Map<K, V>.toMultiValueMap() = mapValues { listOf(it.value?.toString()) }.let(CollectionUtils::toMultiValueMap)

val randomDateTime get() = LocalDateTime.now().plusSeconds(Random.nextLong(7 * 24 * 60 * 60))
val randomType get() = Event.Type.values().random().toString()
val randomUUID get() = UUID.randomUUID().toString()