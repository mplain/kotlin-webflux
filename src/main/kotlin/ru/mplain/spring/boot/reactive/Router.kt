package ru.mplain.spring.boot.reactive

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ServerWebInputException

@Configuration
class Router(val handler: Handler) {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun router1(): RouterFunction<ServerResponse?> {
        return RouterFunctions.route()
                .POST("events") { request -> handler.post(request) }
                .GET("/events") { request -> handler.get(request) }
                .build()
                .filter { request, handler ->
                    handler.handle(request)
                            .doOnError { logger.error(it.message!!.substringBefore("\n")) }
                            .doOnError { decorateServerWebInputException(it) }
                }
    }

    fun decorateServerWebInputException(e: Throwable) {
        if (e is ServerWebInputException && e.reason == "Failed to read HTTP message") {
            val msg = e.mostSpecificCause.message!!.substringBefore("\n")
            throw ServerWebInputException(msg)
        }
    }
}