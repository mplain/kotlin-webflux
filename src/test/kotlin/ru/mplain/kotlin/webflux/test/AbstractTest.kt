package ru.mplain.kotlin.webflux.test

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.CollectionUtils
import ru.mplain.kotlin.webflux.common.EVENT_DATA
import ru.mplain.kotlin.webflux.common.EVENT_TIME
import ru.mplain.kotlin.webflux.common.EVENT_TYPE
import ru.mplain.kotlin.webflux.domain.Event
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "60000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractTest {
    @Autowired
    lateinit var jackson: ObjectMapper
    @Autowired
    lateinit var webTestClient: WebTestClient

    fun Any.toJson() = jackson.writeValueAsString(this)

    fun <K, V> Map<K, V>.toMultiValueMap() = mapValues { listOf(it.value?.toString()) }.let(CollectionUtils::toMultiValueMap)

    fun post(event: Any) = webTestClient
            .post()
            .uri("/event")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(event)
            .exchange()

    fun get(params: Map<String, Any>) = webTestClient
            .get()
            .uri { it.path("/event").queryParams(params.toMultiValueMap()).build() }
            .exchange()

    fun createEvent(
            time: LocalDateTime? = randomDateTime,
            type: String? = randomType,
            data: String? = randomUUID,
            extra: Pair<String, String>? = null
    ) = mapOf(
            EVENT_TIME to time?.toString(),
            EVENT_TYPE to type,
            EVENT_DATA to data
    )
            .run { if (extra != null) plus(extra) else this }
            .toJson()

    val randomDateTime get() = LocalDateTime.now().plusSeconds(Random.nextLong(7 * 24 * 60 * 60))
    val randomType get() = Event.Type.values().random().toString()
    val randomUUID get() = UUID.randomUUID().toString()
}