package kr.co.promptech.webfluxdemo

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Service
class StreamingService(
    @Qualifier("webApplicationContext") private val resourceLoader: ResourceLoader,
    private val videoService: VideoService,
    private val gridFsTemplate: GridFsTemplate,
) {
    fun getVideo(title: String?): Mono<Resource> =
        Mono.fromSupplier {
            resourceLoader.getResource(String.format(FORMAT, title))
        }

    fun getVideo2(title: String): Mono<Resource> =
        videoService.findVideoByTitleLike(title).flatMap { gridFSFile ->
            val contentType = gridFSFile.metadata?.getString("contentType") ?: MediaType.APPLICATION_OCTET_STREAM_VALUE

            val videoStream = videoService.streamVideo(gridFSFile)
            val inputStream = dataBufferToInputStream(videoStream)

            Mono.just(InputStreamResource(inputStream)).map { resource ->
                resource.apply {
                    gridFSFile.filename
                }
            }
        }

    private fun dataBufferToInputStream(dataBufferFlux: Flux<DataBuffer>): InputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()

        dataBufferFlux.subscribe { dataBuffer ->
            val buffer = ByteArray(dataBuffer.readableByteCount())
            dataBuffer.read(buffer)
            byteArrayOutputStream.write(buffer)
            DataBufferUtils.release(dataBuffer)
        }

        return ByteArrayInputStream(byteArrayOutputStream.toByteArray())
    }

    companion object {
        private const val FORMAT = "classpath:videos/%s.mp4"
    }
}
