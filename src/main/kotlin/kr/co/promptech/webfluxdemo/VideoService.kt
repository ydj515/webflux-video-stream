package kr.co.promptech.webfluxdemo

import com.mongodb.client.gridfs.model.GridFSFile
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.IOException

@Service
class VideoService(
    private val gridFsTemplate: GridFsTemplate,
) {
    fun findVideoByTitleLike(title: String): Mono<GridFSFile> {
        val query =
            Query.query(
                Criteria
                    .where("metadata.title")
                    .regex(".*$title.*", "i"),
            )
        return Mono
            .fromCallable { gridFsTemplate.findOne(query) }
            .subscribeOn(Schedulers.boundedElastic())
    }

    fun streamVideo(gridFSFile: GridFSFile): Flux<DataBuffer> {
        val resource = gridFsTemplate.getResource(gridFSFile)
        val inputStream = resource.inputStream
//        inputStream.skip(range.first)

        val bufferFactory = DefaultDataBufferFactory()

        return Flux.create { sink ->
            val buffer = ByteArray(4096)
            var bytesRead: Int
            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val dataBuffer = bufferFactory.allocateBuffer(bytesRead)
                    dataBuffer.write(buffer, 0, bytesRead)
                    sink.next(dataBuffer)
                }
                sink.complete()
            } catch (e: IOException) {
                sink.error(e)
            } finally {
                inputStream.close()
            }
        }
    }
}
