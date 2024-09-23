package kr.co.promptech.webfluxdemo

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Paths

@RestController
class VideoController {
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping("/video/{filename}")
    fun streamVideo(
        @PathVariable filename: String,
    ): Mono<ServerResponse> {
        val resourcePath = Paths.get("src/main/resources/videos/$filename.mp4")
        val resource = ClassPathResource("videos/$filename.mp4")

        return if (resource.exists() && resource.isReadable) {
            val inputStream = resource.inputStream

            ServerResponse
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
                .contentType(MediaType.valueOf("video/mp4")) // 비디오 포맷에 맞게 변경
                .body(Mono.just(inputStream.readAllBytes()), ByteArray::class.java) // InputStream을 ByteArray로 변환
        } else {
            ServerResponse.notFound().build()
        }
    }

    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping(value = ["/video/{filename}"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun streamVideo(
        @PathVariable filename: String,
        response: ServerHttpResponse,
    ): Mono<Void> {
        val resourcePath = Paths.get("src/main/resources/videos/$filename.mp4")
        val fileStream = Files.newInputStream(resourcePath)
        val dataBufferFlux = DataBufferUtils.readInputStream({ fileStream }, response.bufferFactory(), 4096)

        response.headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        response.headers.set(HttpHeaders.TRANSFER_ENCODING, "chunked")

        return response.writeWith(dataBufferFlux)
    }
}
