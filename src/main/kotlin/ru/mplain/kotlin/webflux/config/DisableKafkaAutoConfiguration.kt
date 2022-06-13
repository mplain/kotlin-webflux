package ru.mplain.kotlin.webflux.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import ru.mplain.kotlin.webflux.common.KAFKA

@Configuration
@Profile("!$KAFKA")
@EnableAutoConfiguration(exclude = [KafkaAutoConfiguration::class])
class DisableKafkaAutoConfiguration