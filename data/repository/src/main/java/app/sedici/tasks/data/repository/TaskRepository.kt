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

package app.sedici.tasks.data.repository

import app.sedici.tasks.data.local.common.daos.TaskDao
import app.sedici.tasks.data.local.common.model.TaskEntity
import app.sedici.tasks.model.NewTask
import app.sedici.tasks.model.Task
import app.sedici.tasks.model.TaskId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject

interface TaskRepository {
    suspend fun saveNewTask(newTask: NewTask): TaskId

    fun observeTasks(): Flow<List<Task>>
}

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTaskRepository @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override suspend fun saveNewTask(newTask: NewTask): TaskId {
        val entity = TaskEntity(
            title = newTask.title,
            description = newTask.description,
            isChecked = false,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
            expiresOn = newTask.expiresOn
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.toOffsetDateTime(),
        )

        taskDao.insert(entity)

        return entity.id.toTaskId()
    }

    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeAll().map { entities ->
            entities.map { entity ->
                entity.toTask()
            }
        }
}
