package kr.solve.domain.tag.application.service

import kotlinx.coroutines.flow.map
import kr.solve.domain.tag.domain.entity.Tag
import kr.solve.domain.tag.domain.error.TagError
import kr.solve.domain.tag.domain.repository.TagRepository
import kr.solve.domain.tag.presentation.request.CreateTagRequest
import kr.solve.domain.tag.presentation.response.toResponse
import kr.solve.global.error.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TagService(
    private val tagRepository: TagRepository,
) {
    fun getTags() = tagRepository.findAll().map { it.toResponse() }

    @Transactional
    suspend fun createTag(request: CreateTagRequest) {
        if (tagRepository.existsByName(request.name)) {
            throw BusinessException(TagError.DUPLICATE, request.name)
        }
        tagRepository.save(Tag(name = request.name))
    }

    @Transactional
    suspend fun deleteTag(tagId: UUID) {
        val tag =
            tagRepository.findById(tagId)
                ?: throw BusinessException(TagError.NOT_FOUND)
        tagRepository.delete(tag)
    }
}
