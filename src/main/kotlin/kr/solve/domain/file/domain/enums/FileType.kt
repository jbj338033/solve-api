package kr.solve.domain.file.domain.enums

private val IMAGE_TYPES = setOf("image/jpeg", "image/png", "image/webp")

enum class FileType(
    val path: String,
    val maxSize: Long,
    val contentTypes: Set<String>,
) {
    PROFILE(
        path = "profiles",
        maxSize = 5 * 1024 * 1024,
        contentTypes = IMAGE_TYPES,
    ),
    BANNER(
        path = "banners",
        maxSize = 10 * 1024 * 1024,
        contentTypes = IMAGE_TYPES,
    ),
    PROBLEM_IMAGE(
        path = "problems",
        maxSize = 10 * 1024 * 1024,
        contentTypes = IMAGE_TYPES + "image/gif",
    ),
}
