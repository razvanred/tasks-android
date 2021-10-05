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

import app.sedici.tasks.data.repository.TaskRepository
import app.sedici.tasks.model.Task
import app.sedici.tasks.model.TaskId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTaskById @Inject constructor(
    private val taskRepository: TaskRepository,
) : SubjectInteractor<ObserveTaskById.Params, Task?>() {

    override fun createObservable(params: Params): Flow<Task?> =
        taskRepository.observeTaskById(id = params.id)

    operator fun invoke(id: TaskId) = invoke(Params(id = id))

    data class Params(val id: TaskId)
}
