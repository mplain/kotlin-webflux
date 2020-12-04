package ru.mplain.kotlin.webflux

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.util.CollectionUtils
import ru.mplain.kotlin.webflux.common.EVENT_DATA
import ru.mplain.kotlin.webflux.common.EVENT_TIME
import ru.mplain.kotlin.webflux.common.EVENT_TYPE
import ru.mplain.kotlin.webflux.model.Event
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

val jackson = jacksonObjectMapper()

val randomDateTime get() = LocalDateTime.now().plusSeconds(Random.nextLong(7 * 24 * 60 * 60))
val randomType get() = Event.Type.values().random().toString()
val randomUUID get() = UUID.randomUUID().toString()

fun createEvent(
        vararg extra: Pair<String, String>,
        time: LocalDateTime? = randomDateTime,
        type: String? = randomType,
        data: String? = randomUUID
) = mapOf(
        EVENT_TIME to time?.toString(),
        EVENT_TYPE to type,
        EVENT_DATA to data,
        *extra
).toJson()

fun Any.toJson(): String = jackson.writeValueAsString(this)
fun <K, V> Map<K, V>.toMultiValueMap() = mapValues { listOf(it.value?.toString()) }.let(CollectionUtils::toMultiValueMap)