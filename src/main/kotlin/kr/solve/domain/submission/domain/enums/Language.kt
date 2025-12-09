package kr.solve.domain.submission.domain.enums

enum class Language(
    val extension: String,
    val displayName: String,
) {
    C("c", "C"),
    CPP("cpp", "C++"),
    JAVA("java", "Java"),
    PYTHON("py", "Python"),
    JAVASCRIPT("js", "JavaScript"),
    KOTLIN("kt", "Kotlin"),
    GO("go", "Go"),
    RUST("rs", "Rust"),
}
