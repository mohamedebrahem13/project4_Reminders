package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase
    private lateinit var dao : RemindersDao
    // Executes each task synchronously using Architecture Components.

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @Before
    fun createDataBase(){
        // allow database on main thread just for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        //get dao
        dao = database.reminderDao()
    }

    @Test
    fun saveReminder() = runBlocking{
          // GIVEN create reminder
        val reminder = ReminderDTO(
            "location", "Location Description", "My Location",
            50.0, 60.500
        )

        dao.saveReminder(reminder)
        // WHEN save Reminder that we created
        val loaded  = dao.getReminderById(reminder.id)
         // THEN check if the Reminder was saved
        assertThat(reminder.id, `is`(loaded!!.id))
        assertThat(reminder.title, `is`(loaded.title))
        assertThat(reminder.description, `is`(loaded.description))
        assertThat(reminder.latitude, `is`(loaded.latitude))
        assertThat(reminder.longitude, `is`(loaded.longitude))
    }

    @After
    fun toCloseDB(){
        // after test close data base
        database.close()
    }


}