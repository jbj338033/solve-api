package kr.solve.common.pagination

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "커서 기반 페이지네이션 응답")
data class CursorPage<T>(
    @Schema(description = "데이터 목록")
    val content: List<T>,
    @Schema(description = "다음 페이지 존재 여부")
    val hasNext: Boolean,
) {
    companion object {
        fun <T, R : Any> of(
            items: List<T>,
            size: Int,
            mapper: (T) -> R?,
        ): CursorPage<R> {
            val hasNext = items.size > size
            val content = items.take(size).mapNotNull(mapper)
            return CursorPage(content, hasNext)
        }

        fun <T : Any> of(
            items: List<T>,
            size: Int,
        ): CursorPage<T> = of(items, size) { it }
    }
}
