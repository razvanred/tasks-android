package app.sedici.tasks.data.local.common.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.sedici.tasks.data.local.common.model.TaskEntity.Companion.TableName
import java.time.OffsetDateTime

@Entity(
    tableName = TableName,
)
data class TaskEntity(
    @PrimaryKey
    val id: TaskEntityId,
    val title: String,
    val description: String,
    @ColumnInfo(name = "is_checked")
    val isChecked: Boolean,
    @ColumnInfo(name = "created_at")
    val createdAt: OffsetDateTime,
    @ColumnInfo(name = "updated_at")
    val updatedAt: OffsetDateTime,
    @ColumnInfo(name = "expires_on")
    val expiresOn: OffsetDateTime?,
) {
    companion object {
        const val TableName = "Tasks"
    }
}
