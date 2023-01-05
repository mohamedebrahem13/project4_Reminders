package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

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

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    @Test
    fun reminderSaved_ToastMessage(){
        // start the reminders screen by get the activity that we are testing

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        // idling

        dataBindingIdlingResource.monitorActivity(activityScenario)
           //click on fab button
        onView(withId(R.id.addReminderFAB)).perform(click())
    // click on selected location that's opens the maps
        onView(withId(R.id.selectLocation)).perform(click())
         // long click on the maps
        onView(withId(R.id.map_selected)).perform(ViewActions.longClick())
        //click save to close the map and save the selected location
        onView(withId(R.id.save)).perform(click())

      // so after click save we go back and add title
        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("Title 2"))
      // add description
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText(" Description 2"))
      //click on save reminder
        onView(withId(R.id.saveReminder)).perform(click())
      // check for message
        onView(withText(R.string.reminder_saved))
            .inRoot(
                RootMatchers.withDecorView(
                    CoreMatchers.not(
                        CoreMatchers.`is`(
                            getActivity(
                                activityScenario
                            )!!.window.decorView
                        )
                    )
                )
            )
            .check(matches(isDisplayed()))
        // Make sure the activity is closed before resetting the db:

        activityScenario.close()
    }

    @Test
    fun clickBack() = runBlocking {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //click on fab button
        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("Title 2"))
        // add description
        onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText(" Description 2"))

        // Confirm that if we click back button once, we end up back at the task reminder list page
        onView(isRoot()).perform(ViewActions.pressBack())
        // if addReminderFab is appear so we are in the reminder list page
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))

        // When using ActivityScenario.launch, always call close()
        activityScenario.close()
    }





    @Test
    fun reminderSavedWithNoSelectedLocation_ToastMessage() {
        // start the reminders screen by get the activity that we are testing
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        // idling

        dataBindingIdlingResource.monitorActivity(activityScenario)
        // click on fab button
        onView(withId(R.id.addReminderFAB)).perform(click())
      // put text into edittext
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("Title 1"))
      // close the keyboard
        Espresso.closeSoftKeyboard()
      //click on save button
        onView(withId(R.id.saveReminder)).perform(click())
        // here if we don't put the location the snackbar shows  error with please select  location
        val snackBarMessage = appContext.getString(R.string.err_select_location)
        // check for snackbar
        onView(withText(snackBarMessage))
            .check(matches(isDisplayed()))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()


    }
    @Test
    fun reminderNoTitle(){
        // start the reminders screen by get the activity that we are testing

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        // idling

        dataBindingIdlingResource.monitorActivity(activityScenario)
        // click on the FAB add reminder

        onView(withId(R.id.addReminderFAB))
            .perform(click())

        // put text into edittext

        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("description1"))
        // close the keyboard

        Espresso.closeSoftKeyboard()
        //click on save button

        onView(withId(R.id.saveReminder))
            .perform(click())
        // here if we don't put the title the snackbar shows with please select  title

        val snackBarMessage = appContext.getString(R.string.err_enter_title)
        // check for snackbar

        onView(withText(snackBarMessage))
            .check(matches(isDisplayed()))
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }
    @Test
    fun remindersScreen_clickOnFab_opensSaveReminderScreen() = runBlocking {
        // start the reminders screen by get the activity that we are testing
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        // idling
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // click on the FAB add reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        // check that we are on the SaveReminder screen
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

@After
fun clear()= runBlocking{
        repository.deleteAllReminders()
}

}
