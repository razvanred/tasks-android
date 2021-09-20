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

package app.sedici.tasks.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import app.sedici.tasks.data.local.common.daos.TaskDao
import app.sedici.tasks.data.local.common.model.TaskEntityId
import app.sedici.tasks.data.local.common.testing.createTaskEntity
import app.sedici.tasks.model.NewTask
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class DefaultTaskRepositoryTest {

    @get:Rule
    val hiltRule by lazy { HiltAndroidRule(this) }

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testScope = TestCoroutineScope()

    @Inject
    lateinit var taskDao: TaskDao

    @Inject
    lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun saveNewTask_checkSuccess() = testScope.runBlockingTest {
        val newTask = NewTask(
            title = "Hello world!",
            description = "",
            expiresOn = LocalDate.of(2000, 5, 5)
        )

        val id = taskRepository.saveNewTask(newTask)

        assertThat(taskDao.getByIdOrNull(id = id.toTaskEntityId()))
            .isNotNull()
    }

    @Test
    fun observeAll_checkSuccess() = testScope.runBlockingTest {
        val taskEntity1 = createTaskEntity(
            title = "Play football"
        )
        val taskEntity2 = createTaskEntity(
            title = "Send letter to Santa"
        )
        val task1 = taskEntity1.toTask()
        val task2 = taskEntity2.toTask()

        taskRepository.observeTasks().test {
            taskDao.insert(taskEntity1)
            taskDao.insert(taskEntity2)
            taskDao.delete(taskEntity2)

            assertThat(awaitItem()).isEmpty()
            assertThat(awaitItem()).containsExactly(task1)
            assertThat(awaitItem()).containsExactly(task1, task2)
            assertThat(awaitItem()).containsExactly(task1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteById_checkSuccess() = testScope.runBlockingTest {
        val taskEntity1 = createTaskEntity(title = "Wake up at 8.00AM")
        val taskEntity2 = createTaskEntity(title = "Doctor appointment at 11.00AM")

        taskDao.insert(listOf(taskEntity1, taskEntity2))

        taskRepository.deleteTask(id = taskEntity1.id.toTaskId())

        assertThat(taskDao.getAll()).containsExactly(taskEntity2)
    }

    @Test
    fun delete_checkSuccess() = testScope.runBlockingTest {
        val taskEntity1 = createTaskEntity(title = "Vaccine at 5.00PM")
        val taskEntity2 = createTaskEntity(title = "Buy cat food")

        taskDao.insert(listOf(taskEntity1, taskEntity2))

        taskRepository.deleteTask(task = taskEntity1.toTask())

        assertThat(taskDao.getAll()).containsExactly(taskEntity2)
    }

    @Test
    fun deleteNonExistentById_checkNothingHappens() = testScope.runBlockingTest {
        val taskEntity1 = createTaskEntity(title = "Write bugs")
        val taskEntity2 = createTaskEntity(title = "Deliver buggy product")
        val taskEntity3 = createTaskEntity(title = "Write unit tests")

        taskDao.insert(listOf(taskEntity1, taskEntity2))

        taskRepository.deleteTask(id = taskEntity3.id.toTaskId())

        assertThat(taskDao.getAll()).containsExactly(taskEntity1, taskEntity2)
    }

    @Test
    fun deleteNonExistent_checkNothingHappens() = testScope.runBlockingTest {
        val taskEntity1 = createTaskEntity(title = "Go to the mall")
        val taskEntity2 = createTaskEntity(title = "Invest in tech stocks")
        val taskEntity3 = createTaskEntity(title = "Share wedding photos")

        taskDao.insert(listOf(taskEntity1, taskEntity2))

        taskRepository.deleteTask(task = taskEntity3.toTask())

        assertThat(taskDao.getAll()).containsExactly(taskEntity1, taskEntity2)
    }

    @Test
    fun observeTaskById_checkEmission() = testScope.runBlockingTest {
        val taskEntity1 = createTaskEntity(title = "Go to the prom")
        val taskEntity2 = createTaskEntity(title = "Ask Jessica out")

        taskDao.insert(listOf(taskEntity1, taskEntity2))

        taskRepository.observeTaskById(id = taskEntity1.id.toTaskId()).test {
            assertThat(awaitItem()?.id).isEqualTo(taskEntity1.id.toTaskId())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun observeTaskById_withoutTask_checkNullEmission() = testScope.runBlockingTest {
        taskRepository.observeTaskById(id = TaskEntityId.create().toTaskId()).test {
            assertThat(awaitItem()?.id).isNull()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setTaskDescriptionById_checkSuccess() = testScope.runBlockingTest {
        val taskEntity1 = createTaskEntity(title = "Watch Rick & Morty")
        val taskEntity2 = createTaskEntity(title = "Buy a new monitor")

        taskDao.insert(listOf(taskEntity1, taskEntity2))

        val description = "Only on AdultSwim"

        taskRepository.setTaskDescriptionById(
            id = taskEntity1.id.toTaskId(),
            description = description
        )

        assertThat(taskRepository.getByIdOrNull(id = taskEntity1.id.toTaskId())?.description)
            .isEqualTo(description)
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
