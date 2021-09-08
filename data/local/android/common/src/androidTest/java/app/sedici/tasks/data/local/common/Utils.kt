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
