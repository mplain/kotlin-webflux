package ru.mplain.kotlin.webflux

import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.fu.kofu.mongo.reactiveMongodb
import org.springframework.fu.kofu.reactiveWebApplication
import org.springframework.fu.kofu.webflux.webFlux

fun main(args: Array<String>) {
    app.run(args)
}

val app = reactiveWebApplication {
    beans {
        bean(::router)
        bean<Handler>()
    }
    reactiveMongodb { profile("test") { embedded() } }
    webFlux {
        val serverProperties = javaClass.getDeclaredField("serverProperties")
                .apply { isAccessible = true }
                .get(this) as ServerProperties
        serverProperties.error.includeMessage = ErrorProperties.IncludeAttribute.ALWAYS

        codecs {
            string()
            jackson()
        }
    }
}