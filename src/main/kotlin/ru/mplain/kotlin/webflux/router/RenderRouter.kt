package ru.mplain.kotlin.webflux.router

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class RenderRouter {
    @Bean
    fun renderRoutes() = router {
        GET("/") {
            ServerResponse.ok().render("redirect:/login")
        }

        GET("/login") {
            ServerResponse.ok().render("login")
        }

        POST("/login") { request ->
            request.formData().flatMap {
                ServerResponse.ok().render("login", mapOf("message" to "Hello ${it["username"]?.first()}!"))
            }
        }
    }
}