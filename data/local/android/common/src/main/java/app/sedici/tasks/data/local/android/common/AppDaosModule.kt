package app.sedici.tasks.data.local.android.common

import app.sedici.tasks.data.local.common.UserDatabase
import app.sedici.tasks.data.local.common.daos.TasksDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(AuthSessionComponent::class)
@Module
object UserDaosModule {

    @Provides
    fun provideTasksDao(userDatabase: UserDatabase): TasksDao = userDatabase.tasksDao()
}