package ru.mplain.kotlin.webflux.mongodb

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.server.ServerWebInputException

@Configuration
class MongoRouter {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun mongoRoutes(mongoHandler: MongoHandler) = coRouter {
        POST("/event", mongoHandler::post)
        GET("/event", mongoHandler::get)

        onError<Throwable> { e, _ ->
            logger.error(e.message?.substringBefore("\n"))
            throw e
        }
        onError<IllegalArgumentException> { e, _ ->
            throw ServerWebInputException(e.message ?: "Bad request")
        }
        onError<ServerWebInputException> { e, _ ->
            e as ServerWebInputException
            val msg = e.rootCause?.message?.substringBefore("\n") ?: e.reason ?: "Validation error"
            throw ServerWebInputException(msg)
        }
    }
}