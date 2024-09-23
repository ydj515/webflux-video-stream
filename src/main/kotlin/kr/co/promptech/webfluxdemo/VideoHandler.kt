package catchweak.web.video

import com.mongodb.client.gridfs.model.GridFSFile
import kr.co.promptech.webfluxdemo.VideoService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URLEncoder

@Component
class VideoHandler(
    private val videoService: VideoService,
) {
    fun streamVideo(request: ServerRequest): Mono<ServerResponse> {
        val title = request.pathVariable("title")
        return videoService.findVideoByTitleLike(title).flatMap { gridFSFile ->
            val contentType =
                gridFSFile.metadata?.getString("contentType")
                    ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
            val encodedFilename =
                URLEncoder
                    .encode(
                        gridFSFile.filename,
                        "UTF-8",
                    ).replace("+", "%20")

            val rangeHeader = request.headers().header(HttpHeaders.RANGE).firstOrNull()
            val range = parseRange(rangeHeader, gridFSFile)

            val videoStream = videoService.streamVideo(gridFSFile)

            ServerResponse
                .ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename*=UTF-8''$encodedFilename",
                ).header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(BodyInserters.fromDataBuffers(videoStream))
        }
    }

    private fun parseRange(
        rangeHeader: String?,
        gridFSFile: GridFSFile,
    ): LongRange =
        if (!rangeHeader.isNullOrBlank()) {
            val ranges = rangeHeader.replace("bytes=", "").split("-")
            val start = ranges[0].toLongOrNull() ?: 0L
            val end = ranges.getOrNull(1)?.toLongOrNull() ?: (gridFSFile.length - 1)
            start..end
        } else {
            0L..(gridFSFile.length - 1)
        }
}
