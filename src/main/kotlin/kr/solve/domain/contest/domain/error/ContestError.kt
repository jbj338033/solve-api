package kr.solve.domain.contest.domain.error

import kr.solve.global.error.BaseError
import org.springframework.http.HttpStatus

enum class ContestError(
    override val status: HttpStatus,
    override val message: String,
) : BaseError {
    NOT_FOUND(HttpStatus.NOT_FOUND, "대회를 찾을 수 없습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "대회에 대한 권한이 없습니다"),
    RATED_CONTEST_FORBIDDEN(HttpStatus.FORBIDDEN, "레이팅 대회는 관리자만 생성할 수 있습니다"),
    NOT_STARTED(HttpStatus.BAD_REQUEST, "대회가 시작되지 않았습니다"),
    ENDED(HttpStatus.BAD_REQUEST, "대회가 종료되었습니다"),
    ALREADY_PARTICIPATING(HttpStatus.CONFLICT, "이미 참가중입니다"),
    NOT_PARTICIPATING(HttpStatus.BAD_REQUEST, "참가하지 않은 대회입니다"),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "잘못된 초대 코드입니다"),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "종료 시간은 시작 시간보다 이후여야 합니다"),
}
