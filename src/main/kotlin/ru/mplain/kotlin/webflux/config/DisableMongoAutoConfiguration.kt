package ru.mplain.kotlin.webflux.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import ru.mplain.kotlin.webflux.common.MONGO

@Configuration
@Profile("!$MONGO")
@EnableAutoConfiguration(exclude = [MongoAutoConfiguration::class])
class DisableMongoAutoConfiguration