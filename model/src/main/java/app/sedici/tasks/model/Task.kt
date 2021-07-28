package app.sedici.tasks.model

import java.time.LocalDateTime

data class Task(
    val id: TaskId,
    val title: String,
    val description: String,
    val isChecked: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val expiresOn: LocalDateTime?,
)
