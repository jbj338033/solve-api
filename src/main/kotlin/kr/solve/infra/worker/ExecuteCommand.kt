package kr.solve.infra.worker

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ExecuteCommand.Stdin::class, name = "STDIN"),
    JsonSubTypes.Type(value = ExecuteCommand.Kill::class, name = "KILL"),
)
sealed class ExecuteCommand {
    data class Stdin(
        val data: String,
    ) : ExecuteCommand()

    data object Kill : ExecuteCommand()
}
