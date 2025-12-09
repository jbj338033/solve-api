package kr.solve.common.pagination

data class Page<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    companion object {
        fun <T, R> of(
            items: List<T>,
            page: Int,
            size: Int,
            totalElements: Long,
            mapper: (T) -> R,
        ): Page<R> =
            Page(
                content = items.map(mapper),
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = ((totalElements + size - 1) / size).toInt(),
            )

        fun <T> of(
            items: List<T>,
            page: Int,
            size: Int,
            totalElements: Long,
        ): Page<T> = of(items, page, size, totalElements) { it }
    }
}
