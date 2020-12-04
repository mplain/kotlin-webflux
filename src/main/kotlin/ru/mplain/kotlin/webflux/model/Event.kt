package ru.mplain.kotlin.webflux.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
data class Event(
        @Id
        val id: String? = null,
        val time: LocalDateTime,
        val type: Type,
        val data: String
) : Comparable<Event> {

    override fun compareTo(other: Event) = time.compareTo(other.time)

    enum class Type {
        type1, type2, type3;

        companion object {
            operator fun invoke(name: String) = runCatching { valueOf(name) }
                    .getOrElse { throw IllegalArgumentException("Invalid event type: $name") }
        }
    }
}