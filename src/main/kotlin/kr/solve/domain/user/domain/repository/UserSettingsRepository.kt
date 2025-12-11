package kr.solve.domain.user.domain.repository

import kr.solve.domain.user.domain.entity.UserSettings
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface UserSettingsRepository : CoroutineCrudRepository<UserSettings, UUID>
