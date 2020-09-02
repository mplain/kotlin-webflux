package ru.mplain.kotlin.webflux

import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.logging.LogLevel
import org.springframework.fu.kofu.mongo.reactiveMongodb
import org.springframework.fu.kofu.reactiveWebApplication
import org.springframework.fu.kofu.webflux.webFlux

fun main(args: Array<String>) {
    app.run(args)
}

val app = reactiveWebApplication {
    logging { level = LogLevel.INFO }
    beans {
        bean(::router)
        bean<Handler>()
    }
    reactiveMongodb { embedded() }
    webFlux {
        with(javaClass.getDeclaredField("serverProperties")) {
            isAccessible = true
            val serverProperties = get(this@webFlux) as ServerProperties
            serverProperties.error.includeMessage = ErrorProperties.IncludeAttribute.ALWAYS
        }
        codecs {
            string()
            jackson()
        }
    }
}