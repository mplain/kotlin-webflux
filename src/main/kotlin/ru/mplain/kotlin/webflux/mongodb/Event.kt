package ru.mplain.kotlin.webflux.mongodb

import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class Event(
        val time: LocalDateTime,
        val type: Type,
        val data: String
) : Comparable<Event> {

    override fun compareTo(other: Event) = time.compareTo(other.time)

    enum class Type(private val value: String) {
        TYPE_1("type1"),
        TYPE_2("type2"),
        TYPE_3("type3");

        @JsonValue
        override fun toString() = value

        companion object {
            operator fun invoke(s: String) = values().find { it.value == s }
                    ?: throw IllegalArgumentException("Invalid event type requested: $s")
        }
    }
}