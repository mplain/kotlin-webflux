package ru.mplain.spring.boot.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Document
class Event(@param:JsonProperty(EVENT_TIME, required = true) val time: LocalDateTime,
            @param:JsonProperty(EVENT_TYPE, required = true) val type: Type,
            @param:JsonProperty(EVENT_DATA, required = true) val data: String) : Comparable<Event> {
    companion object {
        const val EVENT_TIME = "time"
        const val EVENT_TYPE = "type"
        const val EVENT_DATA = "data"
    }

    override fun compareTo(other: Event): Int {
        return time.compareTo(other.time)
    }

    override fun toString(): String {
        return """{
            |"$EVENT_TIME":"${time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}",
            |"$EVENT_TYPE":"$type",
            |"$EVENT_DATA":"$data"
            |}""".trimMargin().replace("\n", "")
    }

    enum class Type(private val value: String) {
        TYPE_1("type1"),
        TYPE_2("type2"),
        TYPE_3("type3");

        @JsonValue
        override fun toString(): String {
            return value
        }
    }
}