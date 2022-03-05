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
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.coEvery
import io.mockk.spyk
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
class SetTaskDescriptionByIdTest {

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
        val setTaskDescriptionById = SetTaskDescriptionById(
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
        taskRepository.saveNewTask(newTask = newTask2)

        val description = "It's Saturday today"

        setTaskDescriptionById(
            id = task1Id,
            description = description
        ).test {
            assertThat(awaitItem()).isEqualTo(InvokeStarted)
            assertThat(awaitItem()).isEqualTo(InvokeSuccess)
            cancelAndConsumeRemainingEvents()
        }

        assertThat(taskRepository.getByIdOrNull(id = task1Id)?.description)
            .isEqualTo(description)
    }

    @Test
    fun invoke_withFailingRepository_checkEmitsFailure() = testScope.runBlockingTest {
        val taskRepository = spyk(taskRepository)
        val setTaskDescriptionById = SetTaskDescriptionById(
            taskRepository = taskRepository,
            dispatchers = dispatchers
        )
        val newTask = NewTask(
            title = "Let's go to the mall",
            expiresOn = null,
            description = ""
        )
        val taskId = taskRepository.saveNewTask(newTask = newTask)
        val newDescription = "TODAY"

        coEvery { taskRepository.setTaskDescriptionById(id = taskId, description = newDescription) }
            .throws(RuntimeException("Stub!"))

        setTaskDescriptionById(id = taskId, description = newDescription).test {
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
