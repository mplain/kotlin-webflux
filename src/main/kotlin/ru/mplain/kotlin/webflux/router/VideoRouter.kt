package ru.mplain.kotlin.webflux.router

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.PathResource
import org.springframework.util.ClassUtils
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

@Configuration
class VideoRouter {
    private val logger = LoggerFactory.getLogger(ClassUtils.getUserClass(javaClass))
    private val location = ClassPathResource("video").let { Path.of(it.uri) }

    @Bean
    fun videoRoutes() = router {
        filter { request, function ->
            logger.info("${request.methodName()} ${request.path()}")
            function(request)
        }

        GET("/api/video") {
            val files = location.listDirectoryEntries()
                .map { it.nameWithoutExtension }
                .sorted()
            ServerResponse.ok().bodyValue(files)
        }

        GET("/api/video/{id}") { request ->
            val id = request.pathVariable("id")
            val resource = PathResource("$location/$id.mp4")
            if (resource.exists()) ServerResponse.ok().bodyValue(resource)
            else ServerResponse.notFound().build()
        }
    }
}