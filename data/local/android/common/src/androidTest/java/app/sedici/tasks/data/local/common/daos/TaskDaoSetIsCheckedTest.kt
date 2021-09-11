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
import app.sedici.tasks.base.common.test.coAssertThrows
import app.sedici.tasks.data.local.common.testing.createTaskEntity
import com.google.common.truth.Truth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
import org.junit.runners.Parameterized
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@RunWith(Parameterized::class)
@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TaskDaoSetIsCheckedTest(private val isChecked: Boolean) {

    @get:Rule
    val hiltRule by lazy { HiltAndroidRule(this) }

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var taskDao: TaskDao

    private val testScope = TestCoroutineScope()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun setIsCheckedById_checkSuccess() = testScope.runBlockingTest {
        val task = createTaskEntity(title = "Go to the party", isChecked = !isChecked)
        taskDao.insert(task)

        taskDao.setIsCheckedById(id = task.id, isChecked = isChecked)

        Truth.assertThat(taskDao.getByIdOrNull(task.id)?.isChecked).isEqualTo(isChecked)
    }

    @Test
    fun setIsCheckedById_withFailingRepository_checkSuccess() = testScope.runBlockingTest {
        val task = createTaskEntity(title = "Go to the party", isChecked = !isChecked)
        val taskDao: TaskDao = mockk()
        coEvery { taskDao.insert(task) } returns Unit

        taskDao.insert(task)

        coEvery { taskDao.setIsCheckedById(id = task.id, isChecked = isChecked) }
            .throws(RuntimeException("Stub!"))

        coAssertThrows(RuntimeException::class.java) {
            taskDao.setIsCheckedById(id = task.id, isChecked = isChecked)
        }
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "is_checked")
        fun params() = listOf(true, false)
    }
}
