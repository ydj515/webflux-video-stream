package kr.co.promptech.webfluxdemo.routes

import kr.co.promptech.webfluxdemo.handlers.VideoHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class RouterConfig(
    private val videoHandler: VideoHandler,
) {
    @Bean
    fun sampleRouter(): RouterFunction<ServerResponse> =
        RouterFunctions
            .route()
            .GET("/video/{title}") { serverRequest: ServerRequest ->
                videoHandler.sampleVideoHandler(serverRequest)
            }.build()

    @Bean
    fun withMongoVideoRouter(): RouterFunction<ServerResponse> =
        RouterFunctions
            .route()
            .GET("/videos/search/{title}") { serverRequest: ServerRequest ->
                videoHandler.mongoVideoHandler(serverRequest)
            }.build()
}
