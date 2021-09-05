package app.sedici.tasks.data.local.common

import app.sedici.tasks.data.local.common.model.TaskEntity
import app.sedici.tasks.data.local.common.model.TaskEntityId
import java.time.OffsetDateTime

fun createTaskEntity(
    id: TaskEntityId = TaskEntityId.create(),
    title: String = "",
    description: String = "",
    isChecked: Boolean = false,
    createdAt: OffsetDateTime = OffsetDateTime.now(),
    updatedAt: OffsetDateTime = createdAt,
    expiresOn: OffsetDateTime? = null,
) = TaskEntity(
    id = id,
    title = title,
    description = description,
    isChecked = isChecked,
    createdAt = createdAt,
    updatedAt = updatedAt,
    expiresOn = expiresOn,
)


