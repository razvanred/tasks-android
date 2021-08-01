package app.sedici.tasks.data.local.common.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import app.sedici.tasks.data.local.common.model.TaskEntity

@Dao
interface TasksDao {
    @Insert(onConflict = IGNORE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = IGNORE)
    suspend fun insert(tasks: List<TaskEntity>)

    @Delete
    suspend fun delete(task: TaskEntity)
}
