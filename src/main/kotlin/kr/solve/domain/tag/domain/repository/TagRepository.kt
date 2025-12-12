package kr.solve.domain.tag.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.tag.domain.entity.Tag
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TagRepository : CoroutineCrudRepository<Tag, Long> {
    suspend fun findByName(name: String): Tag?

    suspend fun existsByName(name: String): Boolean

    fun findAllByIdIn(ids: List<Long>): Flow<Tag>
}
