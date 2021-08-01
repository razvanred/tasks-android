/*
 * Copyright 2021 Răzvan Roșu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
