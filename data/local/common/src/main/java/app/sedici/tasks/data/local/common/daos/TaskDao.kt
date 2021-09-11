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

package app.sedici.tasks.data.local.common.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import androidx.room.Update
import app.sedici.tasks.data.local.common.model.TaskEntity
import app.sedici.tasks.data.local.common.model.TaskEntityId
import kotlinx.coroutines.flow.Flow
import org.intellij.lang.annotations.Language
import app.sedici.tasks.data.local.common.model.TaskEntity.Companion.TableName as Tasks

@Dao
interface TaskDao {
    @Insert(onConflict = IGNORE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = IGNORE)
    suspend fun insert(tasks: List<TaskEntity>)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Query(QUERY_GET_ALL)
    suspend fun getAll(): List<TaskEntity>

    @Query(QUERY_GET_BY_ID)
    suspend fun getByIdOrNull(id: TaskEntityId): TaskEntity?

    @Query(QUERY_GET_ALL)
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("DELETE FROM $Tasks WHERE id = :id")
    suspend fun deleteById(id: TaskEntityId)

    @Query("UPDATE $Tasks SET is_checked = :isChecked WHERE id = :id")
    suspend fun setIsCheckedById(id: TaskEntityId, isChecked: Boolean)

    companion object {
        @Language("RoomSql")
        private const val QUERY_GET_BY_ID = """
            SELECT * FROM $Tasks
            WHERE id = :id
        """

        @Language("RoomSql")
        private const val QUERY_GET_ALL = """
            SELECT * FROM $Tasks
            ORDER BY 
                expires_on DESC,
                created_at DESC,
                updated_at DESC,
                title,
                description,
                is_checked,
                id
        """
    }
}
