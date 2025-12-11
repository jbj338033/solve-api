package kr.solve.domain.user.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.solve.domain.user.domain.enums.Gender
import java.time.LocalDate

@Schema(description = "사용자 설정 수정 요청")
data class UpdateUserSettingsRequest(
    @Schema(description = "국가")
    val country: String? = null,
    @Schema(description = "국가 공개 여부")
    val countryVisible: Boolean? = null,
    @Schema(description = "생년월일")
    val birthDate: LocalDate? = null,
    @Schema(description = "생년월일 공개 여부")
    val birthDateVisible: Boolean? = null,
    @Schema(description = "성별")
    val gender: Gender? = null,
    @Schema(description = "기타 성별 (gender=OTHER인 경우)")
    val genderOther: String? = null,
    @Schema(description = "성별 공개 여부")
    val genderVisible: Boolean? = null,
    @Schema(description = "대명사", example = "he/him")
    val pronouns: String? = null,
    @Schema(description = "대명사 공개 여부")
    val pronounsVisible: Boolean? = null,
)
