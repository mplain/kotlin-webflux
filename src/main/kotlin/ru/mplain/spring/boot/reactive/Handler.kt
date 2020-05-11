package ru.mplain.spring.boot.reactive

import com.mongodb.reactivestreams.client.MongoClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.queryParamOrNull
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono
import ru.mplain.spring.boot.domain.Event
import java.util.*
import java.util.stream.Collectors

@Component
class Handler(mongoClient: MongoClient, @Value("\${spring.data.mongodb.database}") database: String) {
    companion object {
        const val QUERY_TYPE = "type"
        const val QUERY_PAGE = "page"
        const val QUERY_SIZE = "size"
    }

    val logger: Logger = LoggerFactory.getLogger(javaClass)
    val repository: ReactiveMongoTemplate = ReactiveMongoTemplate(mongoClient, database)

    fun post(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(Event::class.java)
                .doOnNext { logger.info("POST request received: $it") }
                .flatMap(repository::save)
                .flatMap { ServerResponse.ok().bodyValue(it) }
                .switchIfEmpty(Mono.error(ServerWebInputException("POST request has empty body!")))
    }

    fun get(request: ServerRequest): Mono<ServerResponse> {
        val parseNumber = { s: String -> val n = s.substringAfter("=").toLongOrNull(); if (n == 0L) null else n }
        val type = request.queryParamOrNull(QUERY_TYPE)?.substringAfter("=")
        val page = request.queryParamOrNull(QUERY_PAGE)?.let(parseNumber) ?: 1
        val size = request.queryParamOrNull(QUERY_SIZE)?.let(parseNumber) ?: 100
        logger.info("GET request received (type: ${type ?: "any"}, page: ${page}, items per page: ${size})")
        return repository.findAll(Event::class.java)
                .filter { if (type == null) true else resolveEventType(type) == it.type }
                .sort()
                .skip((page - 1) * size)
                .take(size)
                .collect(Collectors.groupingBy { e: Event -> e.time.toLocalDate() })
                .map { TreeMap(it) }
                .flatMap {
                    if (it.isEmpty()) Mono.error(ServerWebInputException("No events retrieved by request!"))
                    else ServerResponse.ok().bodyValue(it)
                }
    }

    fun resolveEventType(type: String): Event.Type {
        return Event.Type.values().find { it.toString() == type }
                ?: throw ServerWebInputException("Invalid event type requested: $type")
    }
}