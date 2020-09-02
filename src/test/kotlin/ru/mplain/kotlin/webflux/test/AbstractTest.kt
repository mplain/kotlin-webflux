package ru.mplain.kotlin.webflux.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.CollectionUtils
import ru.mplain.kotlin.webflux.app
import ru.mplain.kotlin.webflux.EVENT_DATA
import ru.mplain.kotlin.webflux.EVENT_TIME
import ru.mplain.kotlin.webflux.EVENT_TYPE
import ru.mplain.kotlin.webflux.Event
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractTest {
    private val jackson = jacksonObjectMapper()
    private val webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:8080").build()
    private lateinit var context: ConfigurableApplicationContext

    @BeforeAll
    fun beforeAll() {
        context = app.run(profiles = "test")
        for (i in 1..20) post(createEvent())
    }

    @AfterAll
    fun afterAll() {
        context.close()
    }

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