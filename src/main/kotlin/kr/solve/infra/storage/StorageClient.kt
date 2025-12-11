package kr.solve.infra.storage

import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Component
class StorageClient(
    private val s3Presigner: S3Presigner,
    private val storageProperties: StorageProperties,
) {
    fun createPresignedUploadUrl(
        key: String,
        contentType: String,
        expiresIn: Duration = Duration.ofMinutes(15),
    ): PresignedUploadResult {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(storageProperties.bucket)
            .key(key)
            .contentType(contentType)
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(expiresIn)
            .putObjectRequest(putObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignPutObject(presignRequest)

        return PresignedUploadResult(
            uploadUrl = presignedRequest.url().toString(),
            fileUrl = "${storageProperties.publicUrl}/$key",
            expiresIn = expiresIn.seconds,
        )
    }

    data class PresignedUploadResult(
        val uploadUrl: String,
        val fileUrl: String,
        val expiresIn: Long,
    )
}
