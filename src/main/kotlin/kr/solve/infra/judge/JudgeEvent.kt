package kr.solve.infra.judge

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import kr.solve.domain.submission.domain.enums.JudgeResult

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = JudgeEvent.Progress::class, name = "PROGRESS"),
    JsonSubTypes.Type(value = JudgeEvent.Complete::class, name = "COMPLETE"),
)
sealed class JudgeEvent {
    data class Progress(
        val testcaseId: Long,
        val result: JudgeResult,
        val time: Int,
        val memory: Int,
        val score: Int,
        val progress: Int,
    ) : JudgeEvent()

    data class Complete(
        val result: JudgeResult,
        val score: Int,
        val time: Int,
        val memory: Int,
        val error: String?,
    ) : JudgeEvent()
}
