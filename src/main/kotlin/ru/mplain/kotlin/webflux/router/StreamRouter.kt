package ru.mplain.kotlin.webflux.router

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ClassUtils
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Flux

@Configuration
class StreamRouter {
    private val logger = LoggerFactory.getLogger(ClassUtils.getUserClass(javaClass))
    private lateinit var bridge: (Any) -> Unit

    @Bean
    fun streamRoutes() = router {
        POST("/stream") { request ->
            logger.info("Request received")
            request.bodyToFlux<Any>()
                .doOnNext { logger.info("Server received: $it") }
                .doOnNext(bridge)
                .then(ServerResponse.noContent().build())
        }

        GET("/stream") {
            val flux = Flux.create<Any> { sink -> bridge = { event -> sink.next(event) } }
                .publish()
                .autoConnect()
                .cache(10)
                .doOnNext { logger.info("Server sent: $it") }
            ServerResponse.ok().sse().body(flux)
        }
    }
}