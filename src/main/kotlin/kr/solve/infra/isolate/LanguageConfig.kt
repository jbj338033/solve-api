package kr.solve.infra.isolate

import kr.solve.domain.submission.domain.enums.Language

data class LanguageConfig(
    val sourceFile: String,
    val compileCommand: List<String>?,
    val executeCommand: List<String>,
) {
    companion object {
        private val configs =
            mapOf(
                Language.C to
                    LanguageConfig(
                        sourceFile = "main.c",
                        compileCommand = listOf("/usr/bin/gcc", "-O2", "-o", "main", "main.c", "-lm"),
                        executeCommand = listOf("./main"),
                    ),
                Language.CPP to
                    LanguageConfig(
                        sourceFile = "main.cpp",
                        compileCommand = listOf("/usr/bin/g++", "-O2", "-std=c++17", "-o", "main", "main.cpp"),
                        executeCommand = listOf("./main"),
                    ),
                Language.JAVA to
                    LanguageConfig(
                        sourceFile = "Main.java",
                        compileCommand = listOf("/usr/bin/javac", "Main.java"),
                        executeCommand = listOf("/usr/bin/java", "Main"),
                    ),
                Language.PYTHON to
                    LanguageConfig(
                        sourceFile = "main.py",
                        compileCommand = null,
                        executeCommand = listOf("/usr/bin/python3", "main.py"),
                    ),
                Language.JAVASCRIPT to
                    LanguageConfig(
                        sourceFile = "main.js",
                        compileCommand = null,
                        executeCommand = listOf("/usr/bin/node", "main.js"),
                    ),
                Language.KOTLIN to
                    LanguageConfig(
                        sourceFile = "Main.kt",
                        compileCommand = listOf("/usr/bin/kotlinc", "Main.kt", "-include-runtime", "-d", "Main.jar"),
                        executeCommand = listOf("/usr/bin/java", "-jar", "Main.jar"),
                    ),
                Language.GO to
                    LanguageConfig(
                        sourceFile = "main.go",
                        compileCommand = listOf("/usr/bin/go", "build", "-o", "main", "main.go"),
                        executeCommand = listOf("./main"),
                    ),
                Language.RUST to
                    LanguageConfig(
                        sourceFile = "main.rs",
                        compileCommand = listOf("/usr/bin/rustc", "-O", "-o", "main", "main.rs"),
                        executeCommand = listOf("./main"),
                    ),
            )

        fun of(language: Language): LanguageConfig = configs[language] ?: throw IllegalArgumentException("Unsupported language: $language")
    }
}
