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

package app.sedici.tasks.data.local.common.daos

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sedici.tasks.data.local.common.createTaskEntity
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TaskDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun insertNew_checkSuccess() = runBlocking<Unit> {
        val task = createTaskEntity(
            title = "Sample Title",
            description = "This is a simple description"
        )

        taskDao.insert(task)

        assertThat(taskDao.getAll()).containsExactly(task)
    }

    @Test
    fun insertMultipleNew_checkSuccess() = runBlocking<Unit> {
        val tasks = listOf(
            createTaskEntity(title = "Task 1"),
            createTaskEntity(title = "Task 2"),
        )

        taskDao.insert(tasks)

        assertThat(taskDao.getAll()).containsExactlyElementsIn(tasks)
    }

    @Test
    fun insertMultipleNew_someWithSameId_checkNothingHappens() = runBlocking<Unit> {
        val task1 = createTaskEntity(title = "Task 1")
        val task2 = createTaskEntity(title = "Task 2")
        val task3 = createTaskEntity(title = "Task 3", id = task1.id)

        val tasks = listOf(task1, task2, task3)

        taskDao.insert(tasks)

        assertThat(taskDao.getAll()).containsExactly(task1, task2)
    }

    @Test
    fun insertAlreadyExistent_checkNothingHappens() = runBlocking<Unit> {
        val task = createTaskEntity(
            title = "Sample Title",
            description = "This is a simple description"
        )

        taskDao.insert(task)
        taskDao.insert(task)

        assertThat(taskDao.getAll()).containsExactly(task)
    }

    @Test
    fun deleteExistent_checkSuccess() = runBlocking<Unit> {
        val task1 = createTaskEntity(
            title = "Hello world!",
            description = "Print it with C++",
        )
        val task2 = createTaskEntity(
            title = "Groceries",
            description = "More pasta this time"
        )
        val task3 = createTaskEntity(title = "Write unit tests")

        taskDao.insert(listOf(task1, task2, task3))

        taskDao.delete(task1)

        assertThat(taskDao.getAll()).containsExactly(task2, task3)
    }

    @Test
    fun deleteNonExistent_checkNothingHappens() = runBlocking<Unit> {
        val task1 = createTaskEntity(
            title = "Hello world!",
            description = "Print it with C++"
        )
        val task2 = createTaskEntity(
            title = "Groceries",
            description = "More pasta this time"
        )
        val nonExistentTask = createTaskEntity(title = "Write unit tests")

        taskDao.insert(listOf(task1, task2))

        taskDao.delete(nonExistentTask)

        assertThat(taskDao.getAll()).containsExactly(task2, task1)
    }

    @Test
    fun updateExistent_checkSuccess() = runBlocking<Unit> {
        val task1 = createTaskEntity(
            title = "Hello world!",
            description = "Print it with C++"
        )
        val task2 = createTaskEntity(
            title = "Groceries",
            description = "More pasta this time"
        )
        val updatedTask2 = task2.copy(description = "More sugar this time")

        taskDao.insert(listOf(task1, task2))

        taskDao.update(updatedTask2)

        assertThat(taskDao.getAll()).containsExactly(updatedTask2, task1)
    }

    @Test
    fun updateNonExistent_checkNothingHappens() = runBlocking<Unit> {
        val task1 = createTaskEntity(
            title = "Hello world!",
            description = "Print it with C++"
        )
        val task2 = createTaskEntity(
            title = "Groceries",
            description = "More pasta this time"
        )
        val task3 = createTaskEntity(description = "Swim class")

        taskDao.insert(listOf(task3, task2))

        taskDao.update(task1)

        assertThat(taskDao.getAll()).containsExactly(task3, task2)
    }
}
