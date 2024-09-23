package kr.co.promptech.webfluxdemo

import com.mongodb.client.gridfs.model.GridFSFile
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.gridfs.ReactiveGridFsOperations
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Service
class StreamingService(
    @Qualifier("webApplicationContext") private val resourceLoader: ResourceLoader,
    private val reactiveGridFsTemplate: ReactiveGridFsTemplate,
    private val gridFsOperations: ReactiveGridFsOperations,
) {
    fun getSampleVideo(title: String?): Mono<Resource> =
        Mono.fromSupplier {
            resourceLoader.getResource(String.format(FORMAT, title))
        }

    fun getMongoVideo(title: String): Mono<Resource> {
        val query = Query(where("metadata.title").regex(".*$title.*", "i")).limit(1)

        return reactiveGridFsTemplate
            .findOne(query)
            .flatMap { gridFSFile ->
                reactiveGridFsTemplate.getResource(gridFSFile)
            }.flatMap { gridFsResource ->
                DataBufferUtils
                    .join(gridFsResource.content)
                    .map { dataBuffer ->
                        val bytes = ByteArray(dataBuffer.readableByteCount())
                        dataBuffer.read(bytes)
                        DataBufferUtils.release(dataBuffer)
                        bytes
                    }.map { bytes ->
                        object : ByteArrayResource(bytes) {
                            override fun getFilename(): String = gridFsResource.filename ?: title
                        } as Resource
                    }
            }.switchIfEmpty(Mono.error(NoSuchElementException("Video not found: $title")))
    }

    fun streamVideo(gridFSFile: GridFSFile): Mono<Resource> =
        reactiveGridFsTemplate
            .getResource(gridFSFile)
            .flatMap { gridFsResource ->
                gridFsResource.inputStream.map { inputStream ->
                    object : InputStreamResource(inputStream) {
                        override fun getFilename(): String = gridFSFile.filename
                    }
                }
            }

    private fun dataBufferToInputStream(dataBufferFlux: Flux<DataBuffer>): InputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()

        return dataBufferFlux
            .collectList()
            .map { dataBuffers ->
                dataBuffers.forEach { dataBuffer ->
                    val buffer = ByteArray(dataBuffer.readableByteCount())
                    dataBuffer.read(buffer)
                    byteArrayOutputStream.write(buffer)
                    DataBufferUtils.release(dataBuffer)
                }
                ByteArrayInputStream(byteArrayOutputStream.toByteArray())
            }.block()!!
    }

    companion object {
        private const val FORMAT = "classpath:videos/%s.mp4"
    }
}
