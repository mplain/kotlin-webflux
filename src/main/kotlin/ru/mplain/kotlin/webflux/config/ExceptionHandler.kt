package ru.mplain.kotlin.webflux.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono
import ru.mplain.kotlin.webflux.common.and

@Component
@Order(-2)
class ExceptionHandler(
        private val jackson: ObjectMapper
) : WebExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handle(exchange: ServerWebExchange, e: Throwable): Mono<Void> {
        val (status, reason) = when (e) {
            is IllegalArgumentException -> HttpStatus.BAD_REQUEST to e.message
            is WebClientResponseException -> e.statusCode to e.getErrorMessage()
            is ResponseStatusException -> e.status to (e.rootCause?.message ?: e.reason)
            else -> throw e
        }
        logger.error(status and reason?.substringBefore("\n"))
        throw ResponseStatusException(status, reason, e)
    }

    private fun WebClientResponseException.getErrorMessage() = runCatching {
        val errorAttributes = jackson.readValue<Map<String, Any?>>(responseBodyAsByteArray)
        val exception = errorAttributes["exception"]?.toString()?.substringAfterLast('.')
        val message = errorAttributes["message"]
        exception and message
    }.getOrDefault(responseBodyAsString)
}