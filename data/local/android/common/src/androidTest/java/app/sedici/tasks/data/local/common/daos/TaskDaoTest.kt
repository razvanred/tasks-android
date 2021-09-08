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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import app.sedici.tasks.data.local.common.createTaskEntity
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TaskDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var taskDao: TaskDao

    private val testScope = TestCoroutineScope()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun insertNew_checkSuccess() = testScope.runBlockingTest {
        val task = createTaskEntity(
            title = "Sample Title",
            description = "This is a simple description"
        )

        taskDao.insert(task)

        assertThat(taskDao.getAll()).containsExactly(task)
    }

    @Test
    fun insertMultipleNew_checkSuccess() = testScope.runBlockingTest {
        val tasks = listOf(
            createTaskEntity(title = "Task 1"),
            createTaskEntity(title = "Task 2"),
        )

        taskDao.insert(tasks)

        assertThat(taskDao.getAll()).containsExactlyElementsIn(tasks)
    }

    @Test
    fun insertMultipleNew_someWithSameId_checkNothingHappens() = testScope.runBlockingTest {
        val task1 = createTaskEntity(title = "Task 1")
        val task2 = createTaskEntity(title = "Task 2")
        val task3 = createTaskEntity(title = "Task 3", id = task1.id)

        val tasks = listOf(task1, task2, task3)

        taskDao.insert(tasks)

        assertThat(taskDao.getAll()).containsExactly(task1, task2)
    }

    @Test
    fun insertAlreadyExistent_checkNothingHappens() = testScope.runBlockingTest {
        val task = createTaskEntity(
            title = "Sample Title",
            description = "This is a simple description"
        )

        taskDao.insert(task)
        taskDao.insert(task)

        assertThat(taskDao.getAll()).containsExactly(task)
    }

    @Test
    fun deleteExistent_checkSuccess() = testScope.runBlockingTest {
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
    fun deleteNonExistent_checkNothingHappens() = testScope.runBlockingTest {
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
    fun updateExistent_checkSuccess() = testScope.runBlockingTest {
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
    fun updateNonExistent_checkNothingHappens() = testScope.runBlockingTest {
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

    @Test
    fun getByIdOrNull_getExistent_checkReturnsNotNull() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Resume my college career",
            description = "I want to do this",
        )
        val task2 = createTaskEntity(title = "Update Sedici Tasks dependencies")

        taskDao.insert(listOf(task1, task2))

        assertThat(taskDao.getByIdOrNull(task1.id))
            .isEqualTo(task1)
    }

    @Test
    fun getByIdOrNull_getNonExistent_checkReturnsNull() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Dentist appointment"
        )
        val task2 = createTaskEntity(title = "Lunch with Luca")

        taskDao.insert(task1)

        assertThat(taskDao.getByIdOrNull(task2.id))
            .isNull()
    }

    @Test
    fun observeAll_insertAndDeleteElements_checkSuccess() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Walk on the moon"
        )
        val task2 = createTaskEntity(
            title = "Surfing",
            description = "California Beach"
        )

        taskDao.observeAll().test {
            assertThat(awaitItem()).isEmpty()

            taskDao.insert(task1)
            assertThat(awaitItem()).containsExactly(task1)

            taskDao.insert(task2)
            assertThat(awaitItem()).containsExactly(task1, task2)

            taskDao.delete(task1)
            assertThat(awaitItem()).containsExactly(task2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
