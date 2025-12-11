package kr.solve.infra.storage

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
@EnableConfigurationProperties(StorageProperties::class)
class StorageConfig(
    private val storageProperties: StorageProperties,
) {
    private val endpoint: URI
        get() = URI.create("https://${storageProperties.accountId}.r2.cloudflarestorage.com")

    private val credentialsProvider: StaticCredentialsProvider
        get() = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(storageProperties.accessKey, storageProperties.secretKey),
        )

    @Bean
    fun s3Client(): S3Client =
        S3Client
            .builder()
            .endpointOverride(endpoint)
            .credentialsProvider(credentialsProvider)
            .region(Region.of("auto"))
            .forcePathStyle(true)
            .build()

    @Bean
    fun s3Presigner(): S3Presigner =
        S3Presigner
            .builder()
            .endpointOverride(endpoint)
            .credentialsProvider(credentialsProvider)
            .region(Region.of("auto"))
            .build()
}
