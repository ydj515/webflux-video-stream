package kr.co.promptech.webfluxdemo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Configuration
class FunctionalEndPointConfig(
    private val streamingService: StreamingService,
) {
    @Bean
    fun router(): RouterFunction<ServerResponse> =
        RouterFunctions
            .route()
            .GET("/video/{title}") { serverRequest: ServerRequest ->
                videoHandler(serverRequest)
            }.build()

    private fun videoHandler(serverRequest: ServerRequest): Mono<ServerResponse> {
        val title = serverRequest.pathVariable("title")
        return ServerResponse
            .ok()
            .contentType(MediaType.valueOf("video/mp4"))
            .body(streamingService.getVideo(title), Resource::class.java)
    }

    @Bean
    fun routerA(): RouterFunction<ServerResponse> =
        RouterFunctions
            .route()
            .GET("/videos/search/{title}") { serverRequest: ServerRequest ->
                videoHandler2(serverRequest)
            }.build()

    private fun videoHandler2(serverRequest: ServerRequest): Mono<ServerResponse> {
        val title = serverRequest.pathVariable("title")
        return ServerResponse
            .ok()
            .contentType(MediaType.valueOf("video/mp4"))
            .body(streamingService.getVideo2(title), Resource::class.java)
    }

    @Bean
    fun webFluxConfigurer(): WebFluxConfigurer =
        object : WebFluxConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry
                    .addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET")
                    .allowedHeaders("*")
            }
        }
}
