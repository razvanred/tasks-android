package app.sedici.tasks.data.local.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.sedici.tasks.data.local.common.model.UserEntity.Companion.TableName

@Entity(
    tableName = TableName,
)
data class UserEntity(
    @PrimaryKey
    val id: UserEntityId,
    val name: String,
) {
    companion object {
        const val TableName = "Users"
    }
}
