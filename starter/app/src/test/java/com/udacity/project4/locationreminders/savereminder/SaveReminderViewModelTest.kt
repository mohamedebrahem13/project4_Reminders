package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
// Set the main coroutines dispatcher for unit testing.
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Use a fake repository to be injected into the view model.
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        stopKoin()
        // Initialise the repository with no reminders.
        fakeDataSource = FakeDataSource()
        val appContext = ApplicationProvider.getApplicationContext() as Application
        saveReminderViewModel = SaveReminderViewModel(appContext, fakeDataSource)
    }

    @Test
    fun whenIncompleteInfo_validationReturnsTrue() {
        // GIVEN
        //  reminder fields if location and title  not null should return true
        saveReminderViewModel.onClear()
        saveReminderViewModel.reminderTitle.value = "location"
        saveReminderViewModel.reminderDescription.value = "some description"
        saveReminderViewModel.reminderSelectedLocationStr.value = "location"
        saveReminderViewModel.longitude.value = 20.0
        saveReminderViewModel.latitude.value = 10.0

        // WHEN - attempting to validate
        val result = saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                saveReminderViewModel.reminderTitle.value,
                saveReminderViewModel.reminderDescription.value,
                saveReminderViewModel.reminderSelectedLocationStr.value,
                saveReminderViewModel.longitude.value,
                saveReminderViewModel.latitude.value,
                "2"
            )
        )

        // THEN - result is false
        // using hamcrest is for more readability
        MatcherAssert.assertThat(result, CoreMatchers.`is`(true))

    }



    @Test
    fun whenIncompleteInfo_validationReturnsNull() {
        // GIVEN
        //  reminder fields if location  is null should return false
        saveReminderViewModel.onClear()
        saveReminderViewModel.reminderTitle.value = "location"
        saveReminderViewModel.reminderDescription.value = "some description"
        saveReminderViewModel.reminderSelectedLocationStr.value = null
        saveReminderViewModel.longitude.value = 15.0
        saveReminderViewModel.latitude.value = 10.0

        // WHEN - attempting to validate
        val result = saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                saveReminderViewModel.reminderTitle.value,
                saveReminderViewModel.reminderDescription.value,
                saveReminderViewModel.reminderSelectedLocationStr.value,
                saveReminderViewModel.longitude.value,
                saveReminderViewModel.latitude.value,
                "1"
            )
        )

        // THEN - result is false
        // using hamcrest is for more readability
        MatcherAssert.assertThat(result, CoreMatchers.`is`(false))

    }



}