package app.sedici.tasks.data.local.android.common

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.sedici.tasks.data.local.common.UserDatabase
import app.sedici.tasks.data.local.common.model.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(SediciTasksTypeConverters::class)
abstract class UserRoomDatabase : RoomDatabase(), UserDatabase
