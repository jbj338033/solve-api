package kr.solve.domain.tag.application.service

import kotlinx.coroutines.flow.map
import kr.solve.domain.tag.domain.repository.TagRepository
import kr.solve.domain.tag.presentation.response.toResponse
import org.springframework.stereotype.Service

@Service
class TagService(
    private val tagRepository: TagRepository,
) {
    fun getTags() = tagRepository.findAll().map { it.toResponse() }
}
