package app.sedici.tasks.data.local.common.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import app.sedici.tasks.data.local.common.model.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = IGNORE)
    suspend fun insert(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)
}
