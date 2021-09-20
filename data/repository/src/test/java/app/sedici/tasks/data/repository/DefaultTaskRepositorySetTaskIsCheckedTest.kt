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
import app.sedici.tasks.data.local.common.daos.TaskDao
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
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@RunWith(ParameterizedRobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class DefaultTaskRepositorySetTaskIsCheckedTest(private val isTaskChecked: Boolean) {

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
    fun setTaskIsChecked_checkSuccess() = testScope.runBlockingTest {
        val newTask = NewTask(
            title = "Solve issue #1232",
            description = "Exceptions thrown everywhere!",
            expiresOn = null
        )

        val id = taskRepository.saveNewTask(newTask)

        taskRepository.setTaskIsCheckedById(id = id, isChecked = isTaskChecked)

        assertThat(taskDao.getByIdOrNull(id = id.toTaskEntityId())?.isChecked)
            .isEqualTo(isTaskChecked)
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "is_task_checked")
        fun params() = listOf(true, false)
    }
}
