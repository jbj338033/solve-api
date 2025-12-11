package kr.solve.infra.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "storage")
data class StorageProperties(
    val accountId: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
    val publicUrl: String,
)
