package ru.mplain.spring.boot.test

import com.fasterxml.jackson.databind.ObjectMapper
import ru.mplain.spring.boot.domain.Event
import ru.mplain.spring.boot.domain.Event.Companion.EVENT_DATA
import ru.mplain.spring.boot.domain.Event.Companion.EVENT_TIME
import ru.mplain.spring.boot.domain.Event.Companion.EVENT_TYPE
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object TestUtils {
    private val jackson = ObjectMapper()

    fun createEvent(time: LocalDateTime?, type: String?, data: String?, vararg other: String): String {
        val map: MutableMap<String, Any> = HashMap()
        time?.let { map[EVENT_TIME] = it.toString() }
        type?.let { map[EVENT_TYPE] = it }
        data?.let { map[EVENT_DATA] = it }
        if (other.isNotEmpty()) map[other[0]] = other[1]
        return jackson.writeValueAsString(map)
    }

    fun createRandomEvent(): String {
        return createEvent(randomDateTime(), randomType(), UUID.randomUUID().toString())
    }

    fun randomDateTime(): LocalDateTime {
        val now = Instant.now()
        val future = now + Duration.ofDays(7)
        val random = (now.epochSecond..future.epochSecond).random()
        val offset = ZoneId.systemDefault().rules.getOffset(now)
        return LocalDateTime.ofEpochSecond(random, 0, offset)
    }

    fun randomType(): String {
        return Event.Type.values().random().toString()
    }
}