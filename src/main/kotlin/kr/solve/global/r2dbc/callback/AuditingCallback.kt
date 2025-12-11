package kr.solve.global.r2dbc.callback

import kr.solve.common.entity.BaseEntity
import org.reactivestreams.Publisher
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class AuditingCallback : BeforeConvertCallback<BaseEntity> {
    override fun onBeforeConvert(entity: BaseEntity, table: SqlIdentifier): Publisher<BaseEntity> {
        val now = LocalDateTime.now()
        if (entity.createdAt == null) {
            entity.createdAt = now
        }
        entity.updatedAt = now
        return Mono.just(entity)
    }
}
