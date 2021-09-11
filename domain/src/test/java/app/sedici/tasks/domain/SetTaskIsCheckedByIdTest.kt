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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@RunWith(ParameterizedRobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@OptIn(ExperimentalTime::class)
class SetTaskIsCheckedByIdTest(private val isChecked: Boolean) {

    private val appDispatchers = AppCoroutineDispatchers(
        main = TestCoroutineDispatcher(),
        io = TestCoroutineDispatcher(),
        computation = Dispatchers.Default
    )

    @get:Rule
    val hiltRule by lazy { HiltAndroidRule(this) }

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testScope = TestCoroutineScope(context = appDispatchers.main)

    @Inject
    lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun invoke_checkSuccess() = testScope.runBlockingTest {
        val id = taskRepository.saveNewTask(
            newTask = NewTask(
                title = "Get vaccinated",
                description = "2nd dose",
                expiresOn = null
            )
        )
        val setTaskIsCheckedById = SetTaskIsCheckedById(
            appDispatchers = appDispatchers,
            taskRepository = taskRepository
        )

        setTaskIsCheckedById(id = id, isChecked = isChecked).test {
            assertThat(awaitItem()).isEqualTo(InvokeStarted)
            assertThat(awaitItem()).isEqualTo(InvokeSuccess)
            awaitComplete()
        }

        assertThat(taskRepository.getByIdOrNull(id = id)?.isChecked).isEqualTo(isChecked)
    }

    @Test
    fun invoke_withFailingRepository_checkReturnsInvokeError() = testScope.runBlockingTest {
        val taskRepository: TaskRepository = spyk(taskRepository)

        val taskId = taskRepository.saveNewTask(
            newTask = NewTask(
                title = "Go to the beach",
                description = "Don't forget the sunscreen",
                expiresOn = null
            )
        )

        coEvery { taskRepository.setTaskIsCheckedById(isChecked = isChecked, id = taskId) }
            .throws(RuntimeException("Stub!"))

        val setTaskIsCheckedById = SetTaskIsCheckedById(
            taskRepository = taskRepository,
            appDispatchers = appDispatchers
        )

        setTaskIsCheckedById(id = taskId, isChecked = isChecked).test {
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

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "is_checked")
        fun params() = listOf(true, false)
    }
}
