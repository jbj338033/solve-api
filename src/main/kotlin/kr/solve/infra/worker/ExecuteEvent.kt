package kr.solve.infra.worker

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ExecuteEvent.Ready::class, name = "READY"),
    JsonSubTypes.Type(value = ExecuteEvent.Stdout::class, name = "STDOUT"),
    JsonSubTypes.Type(value = ExecuteEvent.Stderr::class, name = "STDERR"),
    JsonSubTypes.Type(value = ExecuteEvent.Complete::class, name = "COMPLETE"),
    JsonSubTypes.Type(value = ExecuteEvent.Error::class, name = "ERROR"),
)
sealed class ExecuteEvent {
    data object Ready : ExecuteEvent()

    data class Stdout(
        val data: String,
    ) : ExecuteEvent()

    data class Stderr(
        val data: String,
    ) : ExecuteEvent()

    data class Complete(
        val exitCode: Int,
        val time: Int,
        val memory: Int,
    ) : ExecuteEvent()

    data class Error(
        val message: String,
    ) : ExecuteEvent()
}
