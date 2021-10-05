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
import app.cash.turbine.test
import app.sedici.tasks.data.local.common.testing.createTaskEntity
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class TaskDaoTest {

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
    fun insertNew_checkSuccess() = testScope.runBlockingTest {
        val task = createTaskEntity(
            title = "Sample Title",
            description = "This is a simple description"
        )

        taskDao.insert(task)

        assertThat(taskDao.getAll()).containsExactly(task)
    }

    @Test
    fun insertMultipleNew_checkSuccess() = testScope.runBlockingTest {
        val tasks = listOf(
            createTaskEntity(title = "Task 1"),
            createTaskEntity(title = "Task 2"),
        )

        taskDao.insert(tasks)

        assertThat(taskDao.getAll()).containsExactlyElementsIn(tasks)
    }

    @Test
    fun insertMultipleNew_someWithSameId_checkNothingHappens() = testScope.runBlockingTest {
        val task1 = createTaskEntity(title = "Task 1")
        val task2 = createTaskEntity(title = "Task 2")
        val task3 = createTaskEntity(title = "Task 3", id = task1.id)

        val tasks = listOf(task1, task2, task3)

        taskDao.insert(tasks)

        assertThat(taskDao.getAll()).containsExactly(task1, task2)
    }

    @Test
    fun insertAlreadyExistent_checkNothingHappens() = testScope.runBlockingTest {
        val task = createTaskEntity(
            title = "Sample Title",
            description = "This is a simple description"
        )

        taskDao.insert(task)
        taskDao.insert(task)

        assertThat(taskDao.getAll()).containsExactly(task)
    }

    @Test
    fun deleteExistent_checkSuccess() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Hello world!",
            description = "Print it with C++",
        )
        val task2 = createTaskEntity(
            title = "Groceries",
            description = "More pasta this time"
        )
        val task3 = createTaskEntity(title = "Write unit tests")

        taskDao.insert(listOf(task1, task2, task3))

        taskDao.delete(task1)

        assertThat(taskDao.getAll()).containsExactly(task2, task3)
    }

    @Test
    fun deleteNonExistent_checkNothingHappens() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Hello world!",
            description = "Print it with C++"
        )
        val task2 = createTaskEntity(
            title = "Groceries",
            description = "More pasta this time"
        )
        val nonExistentTask = createTaskEntity(title = "Write unit tests")

        taskDao.insert(listOf(task1, task2))

        taskDao.delete(nonExistentTask)

        assertThat(taskDao.getAll()).containsExactly(task2, task1)
    }

    @Test
    fun updateExistent_checkSuccess() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Hello world!",
            description = "Print it with C++"
        )
        val task2 = createTaskEntity(
            title = "Groceries",
            description = "More pasta this time"
        )
        val updatedTask2 = task2.copy(description = "More sugar this time")

        taskDao.insert(listOf(task1, task2))

        taskDao.update(updatedTask2)

        assertThat(taskDao.getAll()).containsExactly(updatedTask2, task1)
    }

    @Test
    fun updateNonExistent_checkNothingHappens() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Hello world!",
            description = "Print it with C++"
        )
        val task2 = createTaskEntity(
            title = "Groceries",
            description = "More pasta this time"
        )
        val task3 = createTaskEntity(description = "Swim class")

        taskDao.insert(listOf(task3, task2))

        taskDao.update(task1)

        assertThat(taskDao.getAll()).containsExactly(task3, task2)
    }

    @Test
    fun getByIdOrNull_getExistent_checkReturnsNotNull() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Resume my college career",
            description = "I want to do this",
        )
        val task2 = createTaskEntity(title = "Update Sedici Tasks dependencies")

        taskDao.insert(listOf(task1, task2))

        assertThat(taskDao.getByIdOrNull(task1.id))
            .isEqualTo(task1)
    }

    @Test
    fun getByIdOrNull_getNonExistent_checkReturnsNull() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Dentist appointment"
        )
        val task2 = createTaskEntity(title = "Lunch with Luca")

        taskDao.insert(task1)

        assertThat(taskDao.getByIdOrNull(task2.id))
            .isNull()
    }

    @Test
    fun observeAll_insertAndDeleteElements_checkSuccess() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Walk on the moon"
        )
        val task2 = createTaskEntity(
            title = "Surfing",
            description = "California Beach"
        )

        taskDao.observeAll().test {
            assertThat(awaitItem()).isEmpty()

            taskDao.insert(task1)
            assertThat(awaitItem()).containsExactly(task1)

            taskDao.insert(task2)
            assertThat(awaitItem()).containsExactly(task1, task2)

            taskDao.delete(task1)
            assertThat(awaitItem()).containsExactly(task2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteExistentById_checkSuccess() = testScope.runBlockingTest {
        val task1 = createTaskEntity(title = "Watch JoJo Season 6")
        val task2 = createTaskEntity(title = "Explore the nature")

        taskDao.insert(listOf(task1, task2))

        taskDao.deleteById(task1.id)

        assertThat(taskDao.getAll()).containsExactly(task2)
    }

    @Test
    fun deleteNonExistentById_checkNothingHappens() = testScope.runBlockingTest {
        val task1 = createTaskEntity(title = "Watch Interstellar")
        val task2 = createTaskEntity(title = "Solve the hardest math equation")
        val task3 = createTaskEntity(title = "Visit your parents")

        taskDao.insert(listOf(task1, task2))

        taskDao.deleteById(task3.id)

        assertThat(taskDao.getAll()).containsExactly(task1, task2)
    }

    @Test
    fun observeById_checkEmission() = testScope.runBlockingTest {
        val task1 = createTaskEntity(title = "Play with Emma at the park")
        val task2 = createTaskEntity(title = "Buy a bicycle")

        taskDao.insert(listOf(task1, task2))

        taskDao.observeById(task1.id).test {
            assertThat(awaitItem()?.id).isEqualTo(task1.id)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun observeById_withoutTask_checkNullEmission() = testScope.runBlockingTest {
        val task1 = createTaskEntity(title = "Play with Emma at the park")
        val task2 = createTaskEntity(title = "Buy a bicycle")

        taskDao.insert(listOf(task2))

        taskDao.observeById(task1.id).test {
            assertThat(awaitItem()).isNull()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun setDescriptionById_checkSuccess() = testScope.runBlockingTest {
        val task1 = createTaskEntity(title = "Go to the gym")
        val task2 = createTaskEntity(title = "Doctor appointment")

        taskDao.insert(listOf(task1, task2))

        val description = "Get another appointment for the next time"

        taskDao.setDescriptionById(description = description, id = task2.id)

        assertThat(taskDao.getByIdOrNull(id = task2.id)?.description)
            .isEqualTo(description)
    }

    @Test
    fun removeExpirationDateById_checkSuccess() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Hello world",
            expiresOn = OffsetDateTime.of(
                LocalDateTime.of(2020, 1, 6, 0, 0),
                ZoneOffset.UTC
            )
        )

        taskDao.insert(task1)

        taskDao.setExpiresOnById(id = task1.id, expiresOn = null)

        assertThat(taskDao.getByIdOrNull(task1.id)?.expiresOn)
            .isNull()
    }

    @Test
    fun setExpirationDateById_checkSuccess() = testScope.runBlockingTest {
        val task1 = createTaskEntity(
            title = "Hello world",
        )
        val date = OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 6, 0, 0),
            ZoneOffset.UTC
        )

        taskDao.insert(task1)

        taskDao.setExpiresOnById(id = task1.id, expiresOn = date)

        assertThat(taskDao.getByIdOrNull(task1.id)?.expiresOn)
            .isEqualTo(date)
    }

    @Test
    fun setTitleById_checkSuccess() = testScope.runBlockingTest {
        val task1 = createTaskEntity(title = "Hello Java")
        val task2 = createTaskEntity(title = "Moving on")
        val title = "Hello Kotlin"

        taskDao.insert(listOf(task1, task2))

        taskDao.setTitleById(id = task1.id, title = title)

        assertThat(taskDao.getByIdOrNull(id = task1.id)?.title)
            .isEqualTo(title)
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
