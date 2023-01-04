package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repo: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    // Executes each task synchronously using Architecture Components.

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setup(){
        // stop koin to remove 'A Koin Application has already been started'
        stopKoin()
         //  use Koin Library as a service locator
        appContext = ApplicationProvider.getApplicationContext()

        val myModule = module {
            viewModel {
                //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()

                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                //This view model is declared singleton to be used across multiple fragments

                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            // start coin with list of my module
            modules(listOf(myModule))
        }
         // get repository
        repo = get()
        // blocks the current thread until its completion
        // we need this code to run immediately and so we start clear by delete all reminders
        runBlocking {
            repo.deleteAllReminders()
        }

    }
    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }
    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Test
    fun loadReminders_navigate() {
        // fragment that we are going to test
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        // idling
        dataBindingIdlingResource.monitorFragment(scenario)
        // using mock to navigate
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // WHEN - Click on the first list item
        onView(withId(R.id.addReminderFAB)).perform(click())
        // THEN - Verify that we navigate to the first reminder screen
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
    @Test
    fun reminderIsShownInRecyclerView() {
        // same here we are blocking the thread
        runBlocking {
            // GIVEN - one reminder
            val reminder = ReminderDTO(
                "my location",
                "description",
                "location1",
                11.0,
                11.0,
                "random1"
            )
            // save reminder
            repo.saveReminder(reminder)

            // WHEN - ReminderListFragment is displayed
            val scenario =  launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            dataBindingIdlingResource.monitorFragment(scenario)

            // THEN - the reminder is displayed
            onView(withText(reminder.title)).check(matches(isDisplayed()))
            onView(withText(reminder.location)).check(matches(isDisplayed()))
            onView(withText(reminder.description)).check((matches(isDisplayed())))
        }
    }
    @After
    fun tearDown() {
        // stop koin
        stopKoin()
    }
}