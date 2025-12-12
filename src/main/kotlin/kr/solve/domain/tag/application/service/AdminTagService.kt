package kr.solve.domain.tag.application.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.solve.domain.tag.domain.entity.Tag
import kr.solve.domain.tag.domain.error.TagError
import kr.solve.domain.tag.domain.repository.TagRepository
import kr.solve.domain.tag.presentation.response.AdminTagResponse
import kr.solve.domain.tag.presentation.response.toAdminResponse
import kr.solve.global.error.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminTagService(
    private val tagRepository: TagRepository,
) {
    fun getTags(): Flow<AdminTagResponse> = tagRepository.findAll().map { it.toAdminResponse() }

    @Transactional
    suspend fun createTag(name: String) {
        if (tagRepository.existsByName(name)) {
            throw BusinessException(TagError.DUPLICATE, name)
        }
        tagRepository.save(Tag(name = name))
    }

    @Transactional
    suspend fun updateTag(
        tagId: Long,
        name: String,
    ) {
        val tag =
            tagRepository.findById(tagId)
                ?: throw BusinessException(TagError.NOT_FOUND)

        if (tag.name != name && tagRepository.existsByName(name)) {
            throw BusinessException(TagError.DUPLICATE, name)
        }

        tagRepository.save(tag.copy(name = name))
    }

    @Transactional
    suspend fun deleteTag(tagId: Long) {
        val tag =
            tagRepository.findById(tagId)
                ?: throw BusinessException(TagError.NOT_FOUND)
        tagRepository.delete(tag)
    }
}
