package ru.mplain.kotlin.webflux

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.server.ServerWebInputException

fun router(handler: Handler) = coRouter {
    val logger = LoggerFactory.getLogger(object {}.javaClass.packageName + ".Router")

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