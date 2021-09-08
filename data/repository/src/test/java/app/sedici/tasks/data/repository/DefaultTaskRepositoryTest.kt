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
import app.sedici.tasks.base.common.test.coAssertThrows
import app.sedici.tasks.data.local.common.daos.TaskDao
import app.sedici.tasks.model.NewTask
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTaskRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

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
    fun saveNewTask_withFailingTaskDao_checkFailure() = testScope.runBlockingTest {
        val taskDao: TaskDao = mockk()
        coEvery { taskDao.insert(task = any()) }.throws(IOException("Stub!"))
        val taskRepository: TaskRepository = DefaultTaskRepository(taskDao = taskDao)

        val newTask = NewTask(
            title = "Do the laundry",
            description = "Before midnight",
            expiresOn = LocalDate.of(2000, 5, 5)
        )

        coAssertThrows(IOException::class.java) {
            taskRepository.saveNewTask(newTask)
        }
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
