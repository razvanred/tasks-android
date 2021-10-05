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

package app.sedici.tasks.domain

import app.sedici.tasks.base.common.AppCoroutineDispatchers
import app.sedici.tasks.data.repository.TaskRepository
import app.sedici.tasks.model.TaskId
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetTaskTitleById @Inject constructor(
    private val taskRepository: TaskRepository,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<SetTaskTitleById.Params>() {

    override suspend fun doWork(params: Params) = withContext(dispatchers.io) {
        taskRepository.setTaskTitleById(
            id = params.id,
            title = params.title
        )
    }

    operator fun invoke(id: TaskId, title: String) = invoke(Params(id = id, title = title))

    data class Params(val id: TaskId, val title: String)
}
