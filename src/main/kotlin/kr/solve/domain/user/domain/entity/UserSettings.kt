package kr.solve.domain.user.domain.entity

import kr.solve.domain.user.domain.enums.Gender
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.UUID

@Table("user_settings")
data class UserSettings(
    @Id val userId: UUID,
    val country: String? = null,
    val countryVisible: Boolean = true,
    val birthDate: LocalDate? = null,
    val birthDateVisible: Boolean = false,
    val gender: Gender? = null,
    val genderOther: String? = null,
    val genderVisible: Boolean = true,
    val pronouns: String? = null,
    val pronounsVisible: Boolean = true,
)
