package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
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
class RemindersListViewModelTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Use a fake repository to be injected into the view model.
    private lateinit var fakeDataSource: FakeDataSource
    @Before
    fun setupViewModel() {
        stopKoin()
        // Initialise the repository with no reminders.
        fakeDataSource = FakeDataSource()

        // using fake data source in ListViewModel by inject because it's test double
        // becouse we don't have lifecycle observer we use live data util (use life observeForever)
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), fakeDataSource
        )
    }



    @Test
    fun sandbarError() {
        // GIVEN - there's a problem loading reminders
        // Make the repository return errors so the snack is not null
        fakeDataSource.setReturnError(true)

        // WHEN - we want to load rhe reminders
        remindersListViewModel.loadReminders()

        // THEN - It's an error, there's a snackBar
        // using hamcrest is for more readability

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), not(nullValue()))
    }

    @Test
    fun loadReminders_loading() {
        // GIVEN - loading reminders
        //pause the dispatcher
        mainCoroutineRule.pauseDispatcher()
        //get reminders
        remindersListViewModel.loadReminders()

        // WHEN - the dispatcher is paused, showLoading is true
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()

        // THEN - when the dispatcher is resumed, showboating is false
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }



}