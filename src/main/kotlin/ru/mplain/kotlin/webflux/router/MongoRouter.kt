package ru.mplain.kotlin.webflux.router

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.util.ClassUtils
import org.springframework.web.reactive.function.server.*
import ru.mplain.kotlin.webflux.common.EVENT_TIME
import ru.mplain.kotlin.webflux.common.QUERY_PAGE
import ru.mplain.kotlin.webflux.common.QUERY_SIZE
import ru.mplain.kotlin.webflux.common.QUERY_TYPE
import ru.mplain.kotlin.webflux.model.Event
import java.util.*
import java.util.function.Function.identity

@Configuration
class MongoRouter {
    private val logger = LoggerFactory.getLogger(ClassUtils.getUserClass(javaClass))

    @Bean
    fun repository(factory: ReactiveMongoDatabaseFactory) = ReactiveMongoTemplate(factory)

    @Bean
    fun mongoRoutes(repository: ReactiveMongoTemplate) = router {
        POST("/event") { request ->
            val result = request.bodyToFlux<Event>()
                .doOnNext { logger.info("POST request received: $it") }
                .flatMap(repository::save)
                .doOnNext { logger.info("Event saved to database: $it") }
                .doOnError { logger.error("Error saving event to database: ${it.message}") }
            ServerResponse.ok().body(result)
        }

        GET("/event") { request ->
            val page = request.queryParamOrNull(QUERY_PAGE)?.toIntOrNull()?.takeIf { it > 0 } ?: 1
            val size = request.queryParamOrNull(QUERY_SIZE)?.toIntOrNull()?.takeIf { it > 0 } ?: 20
            val type = request.queryParamOrNull(QUERY_TYPE)?.let { Event.Type(it) }
            logger.info("GET request received (type: ${type ?: "any"}, page: ${page}, items per page: ${size})")
            val query = Query().with(PageRequest.of(page - 1, size, Sort.by(EVENT_TIME)))
            if (type != null) query.addCriteria(Criteria.where(QUERY_TYPE).isEqualTo(type))
            repository.find<Event>(query)
                .collectMultimap({ it.time.toLocalDate() }, identity(), { TreeMap() })
                .doOnNext { logger.info("Retrieving ${it.size} events") }
                .flatMap { map ->
                    if (map.isNotEmpty()) ServerResponse.ok().bodyValue(map)
                    else ServerResponse.notFound().build()
                }
        }
    }
}