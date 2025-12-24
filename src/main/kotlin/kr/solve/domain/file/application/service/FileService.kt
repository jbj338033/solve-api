package kr.solve.domain.file.application.service

import kr.solve.domain.file.domain.error.FileError
import kr.solve.domain.file.presentation.request.CreatePresignedUrlRequest
import kr.solve.domain.file.presentation.response.PresignedUrlResponse
import kr.solve.global.error.BusinessException
import kr.solve.infra.storage.StorageClient
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FileService(
    private val storageClient: StorageClient,
) {
    fun createPresignedUrl(request: CreatePresignedUrlRequest): PresignedUrlResponse {
        val fileType = request.type

        if (request.contentType !in fileType.contentTypes) {
            throw BusinessException(FileError.InvalidContentType)
        }

        if (request.size > fileType.maxSize) {
            throw BusinessException(FileError.FileTooLarge)
        }

        val extension = getExtension(request.contentType)
        val key = "${fileType.path}/${UUID.randomUUID()}.$extension"

        val result = storageClient.createPresignedUploadUrl(key, request.contentType)

        return PresignedUrlResponse(
            uploadUrl = result.uploadUrl,
            fileUrl = result.fileUrl,
            expiresIn = result.expiresIn,
        )
    }

    private fun getExtension(contentType: String): String =
        when (contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "bin"
        }
}
