package app.sedici.tasks.data.local.android.common

import androidx.room.TypeConverter
import app.sedici.tasks.data.local.common.model.TaskEntityId
import app.sedici.tasks.data.local.common.model.UserEntityId
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
    fun userEntityIdToValue(userEntityId: UserEntityId?): Long? = userEntityId?.value

    @TypeConverter
    @JvmStatic
    fun userEntityIdFromValue(value: Long?): UserEntityId? = value?.let { UserEntityId(value = it) }

    @TypeConverter
    @JvmStatic
    fun taskEntityIdToValue(taskEntityId: TaskEntityId?): Long? = taskEntityId?.value

    @TypeConverter
    @JvmStatic
    fun taskEntityIdFromValue(value: Long?): TaskEntityId? = value?.let { TaskEntityId(value = it) }
}
