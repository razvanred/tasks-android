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

package app.sedici.tasks.data.local.android.common

import androidx.room.TypeConverter
import app.sedici.tasks.data.local.common.model.AccountEntityId
import app.sedici.tasks.data.local.common.model.TaskEntityId
import java.time.OffsetDateTime

object SediciTasksTypeConverters {

    @TypeConverter
    @JvmStatic
    fun offsetDateTimeFromString(value: String?): OffsetDateTime? =
        if (value != null) OffsetDateTime.parse(value) else null

    @TypeConverter
    @JvmStatic
    fun offsetDateTimeToString(offsetDateTime: OffsetDateTime?): String? =
        offsetDateTime?.toString()

    @TypeConverter
    @JvmStatic
    fun accountEntityIdToValue(accountEntityId: AccountEntityId?): Long? = accountEntityId?.value

    @TypeConverter
    @JvmStatic
    fun accountEntityIdFromValue(value: Long?): AccountEntityId? =
        value?.let { AccountEntityId(value = it) }

    @TypeConverter
    @JvmStatic
    fun taskEntityIdToValue(taskEntityId: TaskEntityId?): Long? = taskEntityId?.value

    @TypeConverter
    @JvmStatic
    fun taskEntityIdFromValue(value: Long?): TaskEntityId? = value?.let { TaskEntityId(value = it) }
}
