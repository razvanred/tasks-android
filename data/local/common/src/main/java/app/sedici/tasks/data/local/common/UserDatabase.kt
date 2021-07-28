package app.sedici.tasks.data.local.common

import app.sedici.tasks.data.local.common.daos.TasksDao

interface UserDatabase {
    fun tasksDao(): TasksDao
}
