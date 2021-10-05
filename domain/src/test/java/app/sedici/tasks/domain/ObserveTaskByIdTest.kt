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
import app.sedici.tasks.data.repository.TaskRepository
import app.sedici.tasks.model.NewTask
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.spyk
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
@OptIn(ExperimentalTime::class)
class ObserveTaskByIdTest {

    @get:Rule
    val hiltRule by lazy { HiltAndroidRule(this) }

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testScope = TestCoroutineScope()

    @Inject
    lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun invoke_checkEmission() = testScope.runBlockingTest {
        val observeTaskById = ObserveTaskById(taskRepository = taskRepository)

        val newTask1 = NewTask(
            title = "Go to the cinema",
            description = "Dune is available for the next 3 days",
            expiresOn = null
        )
        val newTask2 = NewTask(
            title = "Finish Dune book",
            description = "",
            expiresOn = null
        )

        val newTask1Id = taskRepository.saveNewTask(newTask = newTask1)
        taskRepository.saveNewTask(newTask = newTask2)

        observeTaskById(id = newTask1Id)

        observeTaskById.flow.test {
            assertThat(awaitItem()?.id).isEqualTo(newTask1Id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_withoutTask_checkNullEmission() = testScope.runBlockingTest {
        val observeTaskById = ObserveTaskById(taskRepository = taskRepository)

        val newTask1 = NewTask(
            title = "Go to the cinema",
            description = "Dune is available for the next 3 days",
            expiresOn = null
        )

        val newTask1Id = taskRepository.saveNewTask(newTask = newTask1)
        taskRepository.deleteTask(id = newTask1Id)

        observeTaskById(id = newTask1Id)

        observeTaskById.flow.test {
            assertThat(awaitItem()?.id).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_withFailingRepository_checkFailureEmission() = testScope.runBlockingTest {
        val taskRepository: TaskRepository = spyk(taskRepository)

        val newTask1 = NewTask(
            title = "Go to the cinema",
            description = "Dune is available for the next 3 days",
            expiresOn = null
        )

        val newTask1Id = taskRepository.saveNewTask(newTask = newTask1)
        taskRepository.deleteTask(id = newTask1Id)

        every { taskRepository.observeTaskById(id = newTask1Id) }
            .throws(RuntimeException("Stub!"))

        val observeTaskById = ObserveTaskById(taskRepository = taskRepository)

        observeTaskById(newTask1Id)

        observeTaskById.flow.test {
            assertThat(awaitError().message).isEqualTo("Stub!")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
