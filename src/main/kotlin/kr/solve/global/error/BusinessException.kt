package kr.solve.global.error

class BusinessException(
    val error: BaseError,
    vararg args: Any,
) : RuntimeException(error.message.format(*args))
