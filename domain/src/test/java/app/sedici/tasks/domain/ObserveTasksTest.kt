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
import app.sedici.tasks.data.local.common.daos.TaskDao
import app.sedici.tasks.data.repository.TaskRepository
import app.sedici.tasks.model.NewTask
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
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
class ObserveTasksTest {

    @get:Rule
    val hiltRule by lazy { HiltAndroidRule(this) }

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testScope = TestCoroutineScope()

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun invoke_checkSuccess() = testScope.runBlockingTest {
        val observeTasks = ObserveTasks(taskRepository = taskRepository)

        val newTask1 = NewTask(title = "Lie on the sofa", description = "", expiresOn = null)
        val newTask2 = NewTask(
            title = "Finish Androids book",
            description = "Written by Chet Haase",
            expiresOn = null
        )

        observeTasks()

        observeTasks.flow.test {
            val task1Id = taskRepository.saveNewTask(newTask1)
            val task2Id = taskRepository.saveNewTask(newTask2)
            taskRepository.deleteTask(id = task1Id)

            assertThat(awaitItem()).isEmpty()
            assertThat(awaitItem().map { it.id }).containsExactly(task1Id)
            assertThat(awaitItem().map { it.id }).containsExactly(task2Id, task1Id)
            assertThat(awaitItem().map { it.id }).containsExactly(task2Id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_withFailingRepository_checkInvokeErrorResult() = testScope.runBlockingTest {
        val taskRepository: TaskRepository = mockk()

        every { taskRepository.observeTasks() }.throws(RuntimeException("Stub!"))

        val observeTasks = ObserveTasks(taskRepository = taskRepository)

        observeTasks()

        observeTasks.flow.test {
            assertThat(awaitError().message).isEqualTo("Stub!")
        }
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
