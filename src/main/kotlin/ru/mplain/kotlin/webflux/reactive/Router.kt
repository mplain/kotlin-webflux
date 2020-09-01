package ru.mplain.kotlin.webflux.reactive

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.server.ServerWebInputException

@Configuration
class Router {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun router1(handler: Handler) = coRouter {
        POST("/event", handler::post)
        GET("/event", handler::get)
        onError<Throwable> { e, _ ->
            logger.error(e.message?.substringBefore("\n"))
            throw e
        }
        onError<IllegalArgumentException> { e, _ ->
            throw ServerWebInputException(e.message ?: "")
        }
        onError<ServerWebInputException> { e, _ ->
            e as ServerWebInputException
            val msg = e.mostSpecificCause.message?.substringBefore("\n") ?: e.message
            throw ServerWebInputException(msg)
        }
    }
}