package app.sedici.tasks.data.local.android.common

import androidx.room.TypeConverter
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
}
