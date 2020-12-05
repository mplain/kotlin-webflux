package ru.mplain.kotlin.webflux

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import ru.mplain.kotlin.webflux.config.KafkaConfig
import ru.mplain.kotlin.webflux.router.KafkaRouter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableAutoConfiguration(exclude = [KafkaAutoConfiguration::class, MongoReactiveAutoConfiguration::class])
@MockBean(KafkaConfig::class, KafkaRouter::class, ReactiveMongoDatabaseFactory::class)
class RenderTest {
    @Test
    fun test() {
        println("Application running")
        while (true) {
        }
    }
}