package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository
    private lateinit var remindersDAO: RemindersDao
    // Executes each task synchronously using Architecture Components.

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //create data base and local repository
    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remindersDAO = database.reminderDao()
        repository =
            RemindersLocalRepository(
                remindersDAO
            )
    }

    @Test
    fun getReminders() = runBlocking {
        //create two reminder object
        val reminder = ReminderDTO(
            "location", "location Description", "00 Location",
            20.000, 32.000
        )

        val reminder2 = ReminderDTO(
            "location2", "location Description2", "002 Location",
            21.000, 31.000
        )
        // add two reminder using repository
        repository.saveReminder(reminder)
        repository.saveReminder(reminder2)

        //get saved reminder
        val reminders = repository.getReminders()
        // check if result success
        TestCase.assertTrue(reminders is Result.Success)
             // casting
        reminders as Result.Success
        //check if reminders not empty list
        TestCase.assertTrue(reminders.data.isNotEmpty())
    }

    @Test
    fun error() = runBlocking {
        //create two reminder object

        val reminder = ReminderDTO(
            "Title", "Description", " Location",
            20.210, 20.120
        )
        val reminder2 = ReminderDTO(
            "location2", "location Description2", "002 Location",
            21.000, 31.000
        )
        // add two reminder using repository

        repository.saveReminder(reminder)
        //delete all reminders
        repository.deleteAllReminders()
        // check if reminder still in data base but we delete all reminder so that cause error
        val loaded = repository.getReminder(reminder.id)
        TestCase.assertTrue (loaded is Result.Error)
        loaded as Result.Error
        TestCase.assertTrue (loaded.message == "Reminder not found!")
        val loaded2 = repository.getReminder(reminder.id)
        TestCase.assertTrue (loaded2 is Result.Error)
        loaded2 as Result.Error
        TestCase.assertTrue (loaded2.message == "Reminder not found!")

    }



    @After
    // after test close data base
    fun toCloseDB(){
        // after test close data base
        database.close()
    }}