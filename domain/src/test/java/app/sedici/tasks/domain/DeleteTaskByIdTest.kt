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

package app.sedici.tasks.domain

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import app.sedici.tasks.base.common.AppCoroutineDispatchers
import app.sedici.tasks.base.common.InvokeError
import app.sedici.tasks.base.common.InvokeStarted
import app.sedici.tasks.base.common.InvokeSuccess
import app.sedici.tasks.data.repository.TaskRepository
import app.sedici.tasks.model.NewTask
import app.sedici.tasks.model.TaskId
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
class DeleteTaskByIdTest {

    private val dispatchers = AppCoroutineDispatchers(
        main = TestCoroutineDispatcher(),
        io = TestCoroutineDispatcher(),
        computation = TestCoroutineDispatcher()
    )

    private val testScope = TestCoroutineScope(context = dispatchers.main)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val hiltRule by lazy {
        HiltAndroidRule(this)
    }

    @Inject
    lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun invoke_checkSuccess() = testScope.runBlockingTest {
        val deleteTaskById = DeleteTaskById(
            dispatchers = dispatchers,
            taskRepository = taskRepository
        )
        val newTask1 = NewTask(
            title = "Get up late",
            description = "It's Sunday today",
            expiresOn = null
        )
        val newTask2 = NewTask(
            title = "Go to the skate park",
            expiresOn = null,
            description = "You will find Amanda there"
        )
        val task1Id = taskRepository.saveNewTask(newTask = newTask1)
        val task2Id = taskRepository.saveNewTask(newTask = newTask2)

        deleteTaskById(taskId = task1Id).test {
            assertThat(awaitItem()).isEqualTo(InvokeStarted)
            assertThat(awaitItem()).isEqualTo(InvokeSuccess)
            awaitComplete()
        }

        taskRepository.observeTasks().test {
            assertThat(awaitItem().map { it.id })
                .containsExactly(task2Id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_nonExistentId_checkNothingHappens() = testScope.runBlockingTest {
        val deleteTaskById = DeleteTaskById(
            dispatchers = dispatchers,
            taskRepository = taskRepository
        )
        val newTask1 = NewTask(
            title = "Get up late",
            description = "It's Sunday today",
            expiresOn = null
        )
        val newTask2 = NewTask(
            title = "Go to the skate park",
            expiresOn = null,
            description = "You will find Amanda there"
        )
        val task1Id = taskRepository.saveNewTask(newTask = newTask1)
        val task2Id = taskRepository.saveNewTask(newTask = newTask2)

        val fakeTaskId = TaskId(value = "687ef407-f6c6-442c-84be-f7ba0195d9d7")

        deleteTaskById(taskId = fakeTaskId).test {
            assertThat(awaitItem()).isEqualTo(InvokeStarted)
            assertThat(awaitItem()).isEqualTo(InvokeSuccess)
            awaitComplete()
        }

        taskRepository.observeTasks().test {
            assertThat(awaitItem().map { it.id })
                .containsExactly(task2Id, task1Id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_withFailingRepository_checkEmitsFailure() = testScope.runBlockingTest {
        val taskRepository: TaskRepository = mockk()
        val deleteTaskById = DeleteTaskById(
            taskRepository = taskRepository,
            dispatchers = dispatchers
        )
        val taskId = TaskId(value = "26ca2f97-e3c9-4415-99f8-3d9cbaed7a1f")

        coEvery { taskRepository.deleteTask(id = taskId) }.throws(RuntimeException("Stub!"))

        deleteTaskById(taskId).test {
            assertThat(awaitItem()).isEqualTo(InvokeStarted)
            val result = awaitItem()
            assertThat(result).isInstanceOf(InvokeError::class.java)
            assertThat((result as InvokeError).throwable.message).isEqualTo("Stub!")
            awaitComplete()
        }
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
