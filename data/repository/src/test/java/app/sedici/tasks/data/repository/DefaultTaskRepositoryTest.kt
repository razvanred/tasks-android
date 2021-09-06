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

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.sedici.tasks.data.local.common.daos.TaskDao
import app.sedici.tasks.model.NewTask
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class DefaultTaskRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var taskDao: TaskDao

    @Inject
    lateinit var taskRepository: TaskRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun saveNewTask_checkSuccess() = runBlocking {
        val newTask = NewTask(
            title = "Hello world!",
            description = "",
            expiresOn = null
        )

        val id = taskRepository.saveNewTask(newTask)

        assertThat(taskDao.getByIdOrNull(id = id.toTaskEntityId()))
            .isNotNull()
    }

    @Test(expected = Exception::class)
    fun saveNewTask_withFailingTaskDao_checkFailure() = runBlocking<Unit> {
        val taskDao = spyk(taskDao)

        val newTask = NewTask(
            title = "Do the laundry",
            description = "Before midnight",
            expiresOn = LocalDate.of(2000, 19, 5)
        )

        coEvery { taskDao.insert(task = any()) }.throws(RuntimeException("Stub!"))

        taskRepository.saveNewTask(newTask)
    }
}
