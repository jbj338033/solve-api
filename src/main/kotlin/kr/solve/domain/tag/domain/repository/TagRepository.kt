package kr.solve.domain.tag.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.tag.domain.entity.Tag
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface TagRepository : CoroutineCrudRepository<Tag, UUID> {
    suspend fun findByName(name: String): Tag?

    suspend fun existsByName(name: String): Boolean

    fun findAllByIdIn(ids: List<UUID>): Flow<Tag>
}
