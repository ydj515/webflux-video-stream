package kr.co.promptech.webfluxdemo.handlers

import kr.co.promptech.webfluxdemo.services.StreamingService
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class VideoHandler(
    private val streamingService: StreamingService,
) {
    fun sampleVideoHandler(serverRequest: ServerRequest): Mono<ServerResponse> {
        val title = serverRequest.pathVariable("title")
        return ServerResponse
            .ok()
            .contentType(MediaType.valueOf("video/mp4"))
            .body(streamingService.getSampleVideo(title), Resource::class.java)
    }

    fun mongoVideoHandler(serverRequest: ServerRequest): Mono<ServerResponse> {
        val title = serverRequest.pathVariable("title")
        return streamingService
            .getMongoVideo(title)
            .flatMap { resource ->
                ServerResponse
                    .ok()
                    .contentType(MediaType.valueOf("video/mp4"))
                    .bodyValue(resource)
            }
    }
}
