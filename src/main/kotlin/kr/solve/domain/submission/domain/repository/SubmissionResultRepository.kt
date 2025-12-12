package kr.solve.domain.submission.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.solve.domain.submission.domain.entity.SubmissionResult
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SubmissionResultRepository : CoroutineCrudRepository<SubmissionResult, Long> {
    fun findAllBySubmissionId(submissionId: Long): Flow<SubmissionResult>

    suspend fun countBySubmissionId(submissionId: Long): Int

    suspend fun deleteAllBySubmissionId(submissionId: Long)
}
