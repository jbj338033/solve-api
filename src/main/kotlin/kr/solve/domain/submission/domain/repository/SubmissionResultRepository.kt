package kr.solve.domain.submission.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.submission.domain.entity.SubmissionResult
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface SubmissionResultRepository : CoroutineCrudRepository<SubmissionResult, UUID> {
    fun findAllBySubmissionId(submissionId: UUID): Flow<SubmissionResult>

    suspend fun countBySubmissionId(submissionId: UUID): Int

    suspend fun deleteAllBySubmissionId(submissionId: UUID)
}
