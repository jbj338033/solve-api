package kr.solve.infra.isolate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kr.solve.domain.submission.domain.enums.Language
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path

@Component
class IsolateExecutor(
    @Value("\${isolate.max-boxes:16}") private val maxBoxes: Int,
) {
    private val semaphore = Semaphore(maxBoxes)
    private val boxPool = ArrayDeque((0 until maxBoxes).toList())

    suspend fun execute(
        language: Language,
        code: String,
        input: String,
        timeLimit: Int,
        memoryLimit: Int,
    ): IsolateResult =
        withContext(Dispatchers.IO) {
            withBox { boxId ->
                executeInBox(boxId, language, code, input, timeLimit, memoryLimit)
            }
        }

    suspend fun executeInteractive(
        language: Language,
        code: String,
        timeLimit: Int,
        memoryLimit: Int,
        inputChannel: ReceiveChannel<String>,
        onOutput: suspend (String) -> Unit,
        onError: suspend (String) -> Unit,
        onComplete: suspend (IsolateResult) -> Unit,
    ) = withContext(Dispatchers.IO) {
        withBox { boxId ->
            executeInteractiveInBox(boxId, language, code, timeLimit, memoryLimit, inputChannel, onOutput, onError, onComplete)
        }
    }

    suspend fun compile(
        language: Language,
        code: String,
    ): CompileResult =
        withContext(Dispatchers.IO) {
            withBox { boxId ->
                compileInBox(boxId, language, code)
            }
        }

    fun runWithCompiledBox(
        boxId: Int,
        language: Language,
        input: String,
        timeLimit: Int,
        memoryLimit: Int,
    ): IsolateResult {
        val config = LanguageConfig.of(language)
        val boxDir = Path.of("/var/local/lib/isolate/$boxId/box")
        Files.writeString(boxDir.resolve("input.txt"), input)
        return runInBox(boxId, config.executeCommand, timeLimit / 1000.0, memoryLimit, "input.txt")
    }

    fun cleanupBox(boxId: Int) {
        ProcessBuilder("isolate", "--cg", "-b", boxId.toString(), "--cleanup").start().waitFor()
    }

    private suspend fun <T> withBox(block: suspend (Int) -> T): T =
        semaphore.withPermit {
            val boxId = synchronized(boxPool) { boxPool.removeFirst() }
            try {
                block(boxId)
            } finally {
                synchronized(boxPool) { boxPool.addLast(boxId) }
            }
        }

    private fun executeInBox(
        boxId: Int,
        language: Language,
        code: String,
        input: String,
        timeLimit: Int,
        memoryLimit: Int,
    ): IsolateResult {
        val config = LanguageConfig.of(language)
        val boxPath = initBox(boxId)

        try {
            val boxDir = Path.of(boxPath, "box")
            Files.writeString(boxDir.resolve(config.sourceFile), code)
            Files.writeString(boxDir.resolve("input.txt"), input)

            if (config.compileCommand != null) {
                val compileResult = runInBox(boxId, config.compileCommand, 30.0, 512 * 1024, null)
                if (!compileResult.success) {
                    return IsolateResult(
                        success = false,
                        stdout = "",
                        stderr = compileResult.stderr,
                        exitCode = compileResult.exitCode,
                        time = 0,
                        memory = 0,
                        status = IsolateStatus.RUNTIME_ERROR,
                        message = "Compile Error",
                    )
                }
            }

            return runInBox(boxId, config.executeCommand, timeLimit / 1000.0, memoryLimit, "input.txt")
        } finally {
            cleanupBox(boxId)
        }
    }

    private suspend fun executeInteractiveInBox(
        boxId: Int,
        language: Language,
        code: String,
        timeLimit: Int,
        memoryLimit: Int,
        inputChannel: ReceiveChannel<String>,
        onOutput: suspend (String) -> Unit,
        onError: suspend (String) -> Unit,
        onComplete: suspend (IsolateResult) -> Unit,
    ) {
        val config = LanguageConfig.of(language)
        val boxPath = initBox(boxId)

        try {
            val boxDir = Path.of(boxPath, "box")
            Files.writeString(boxDir.resolve(config.sourceFile), code)

            if (config.compileCommand != null) {
                val compileResult = runInBox(boxId, config.compileCommand, 30.0, 512 * 1024, null)
                if (!compileResult.success) {
                    onError(compileResult.stderr)
                    onComplete(
                        IsolateResult(
                            success = false,
                            stdout = "",
                            stderr = compileResult.stderr,
                            exitCode = compileResult.exitCode,
                            time = 0,
                            memory = 0,
                            status = IsolateStatus.RUNTIME_ERROR,
                            message = "Compile Error",
                        ),
                    )
                    return
                }
            }

            runInteractiveInBox(boxId, config.executeCommand, timeLimit / 1000.0, memoryLimit, inputChannel, onOutput, onError, onComplete)
        } finally {
            cleanupBox(boxId)
        }
    }

    private fun compileInBox(
        boxId: Int,
        language: Language,
        code: String,
    ): CompileResult {
        val config = LanguageConfig.of(language)
        val boxPath = initBox(boxId)

        try {
            val boxDir = Path.of(boxPath, "box")
            Files.writeString(boxDir.resolve(config.sourceFile), code)

            if (config.compileCommand == null) {
                return CompileResult(success = true, error = null, boxPath = boxPath, boxId = boxId)
            }

            val result = runInBox(boxId, config.compileCommand, 30.0, 512 * 1024, null)
            return if (result.success) {
                CompileResult(success = true, error = null, boxPath = boxPath, boxId = boxId)
            } else {
                cleanupBox(boxId)
                CompileResult(success = false, error = result.stderr, boxPath = null, boxId = null)
            }
        } catch (e: Exception) {
            cleanupBox(boxId)
            throw e
        }
    }

    private fun initBox(boxId: Int): String {
        val process =
            ProcessBuilder("isolate", "--cg", "-b", boxId.toString(), "--init")
                .redirectErrorStream(true)
                .start()
        val output =
            process.inputStream
                .bufferedReader()
                .readText()
                .trim()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw RuntimeException("Failed to init isolate box $boxId: $output")
        }
        return output
    }

    private fun runInBox(
        boxId: Int,
        command: List<String>,
        timeLimitSec: Double,
        memoryLimit: Int,
        stdin: String?,
    ): IsolateResult {
        val metaFile = Files.createTempFile("isolate-meta-", ".txt")

        try {
            val args =
                mutableListOf(
                    "isolate",
                    "--cg",
                    "-b",
                    boxId.toString(),
                    "-M",
                    metaFile.toString(),
                    "-t",
                    String.format("%.1f", timeLimitSec),
                    "-w",
                    String.format("%.1f", timeLimitSec * 2 + 1),
                    "-x",
                    "0.5",
                    "--cg-mem=${memoryLimit * 1024}",
                    "-p",
                    "-o",
                    "stdout.txt",
                    "-r",
                    "stderr.txt",
                )

            if (stdin != null) {
                args.addAll(listOf("-i", stdin))
            }

            args.addAll(listOf("--run", "--"))
            args.addAll(command)

            ProcessBuilder(args).redirectErrorStream(true).start().waitFor()

            val boxDir = Path.of("/var/local/lib/isolate/$boxId/box")
            val stdout = runCatching { Files.readString(boxDir.resolve("stdout.txt")) }.getOrDefault("")
            val stderr = runCatching { Files.readString(boxDir.resolve("stderr.txt")) }.getOrDefault("")

            return parseMetaFile(metaFile, stdout, stderr)
        } finally {
            Files.deleteIfExists(metaFile)
        }
    }

    private suspend fun runInteractiveInBox(
        boxId: Int,
        command: List<String>,
        timeLimitSec: Double,
        memoryLimit: Int,
        inputChannel: ReceiveChannel<String>,
        onOutput: suspend (String) -> Unit,
        onError: suspend (String) -> Unit,
        onComplete: suspend (IsolateResult) -> Unit,
    ) {
        val metaFile = Files.createTempFile("isolate-meta-", ".txt")

        try {
            val args =
                mutableListOf(
                    "isolate",
                    "--cg",
                    "-b",
                    boxId.toString(),
                    "-M",
                    metaFile.toString(),
                    "-t",
                    String.format("%.1f", timeLimitSec),
                    "-w",
                    String.format("%.1f", timeLimitSec * 2 + 1),
                    "-x",
                    "0.5",
                    "--cg-mem=${memoryLimit * 1024}",
                    "-p",
                    "--run",
                    "--",
                )
            args.addAll(command)

            val process =
                ProcessBuilder(args)
                    .directory(Path.of("/var/local/lib/isolate/$boxId/box").toFile())
                    .start()

            val stdoutJob =
                CoroutineScope(Dispatchers.IO).launch {
                    val buffer = CharArray(1024)
                    while (true) {
                        val read = process.inputStream.bufferedReader().read(buffer)
                        if (read == -1) break
                        onOutput(String(buffer, 0, read))
                    }
                }

            val stderrJob =
                CoroutineScope(Dispatchers.IO).launch {
                    val buffer = CharArray(1024)
                    while (true) {
                        val read = process.errorStream.bufferedReader().read(buffer)
                        if (read == -1) break
                        onError(String(buffer, 0, read))
                    }
                }

            val stdinWriter = process.outputStream.bufferedWriter()
            val stdinJob =
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        for (input in inputChannel) {
                            if (!process.isAlive) break
                            stdinWriter.write(input)
                            stdinWriter.flush()
                        }
                    } catch (_: Exception) {
                    } finally {
                        runCatching { stdinWriter.close() }
                    }
                }

            withContext(Dispatchers.IO) { process.waitFor() }

            stdinJob.cancel()
            stdoutJob.join()
            stderrJob.join()

            onComplete(parseMetaFile(metaFile, "", ""))
        } finally {
            Files.deleteIfExists(metaFile)
        }
    }

    private fun parseMetaFile(
        metaFile: Path,
        stdout: String,
        stderr: String,
    ): IsolateResult {
        val meta =
            Files
                .readAllLines(metaFile)
                .filter { it.contains(":") }
                .associate { it.split(":", limit = 2).let { (k, v) -> k to v } }

        val statusCode = meta["status"]
        val exitCode = meta["exitcode"]?.toIntOrNull() ?: 0
        val time = ((meta["time"]?.toDoubleOrNull() ?: 0.0) * 1000).toInt()
        val memory = meta["cg-mem"]?.toIntOrNull() ?: meta["max-rss"]?.toIntOrNull() ?: 0
        val message = meta["message"]

        val status =
            when (statusCode) {
                "TO" -> IsolateStatus.TIME_LIMIT_EXCEEDED
                "SG" -> if (meta["exitsig"] == "9") IsolateStatus.MEMORY_LIMIT_EXCEEDED else IsolateStatus.RUNTIME_ERROR
                "RE" -> IsolateStatus.RUNTIME_ERROR
                "XX" -> IsolateStatus.INTERNAL_ERROR
                null -> if (exitCode == 0) IsolateStatus.OK else IsolateStatus.RUNTIME_ERROR
                else -> IsolateStatus.RUNTIME_ERROR
            }

        return IsolateResult(
            success = status == IsolateStatus.OK && exitCode == 0,
            stdout = stdout,
            stderr = stderr,
            exitCode = exitCode,
            time = time,
            memory = memory,
            status = status,
            message = message,
        )
    }
}
