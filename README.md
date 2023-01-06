# Location Reminder

A Todo list app with location reminders that remind the user to do something when he reaches a specific location. The app will require the user to create an account and login to set and access reminders.

## Getting Started

1. Clone the project to your local machine.
2. Open the project using Android Studio.

### Dependencies

```
1. A created project on Firebase console.
2. A create a project on Google console.
```

### Installation

Step by step explanation of how to get a dev environment running.

```
1. To enable Firebase Authentication:
        a. Go to the authentication tab at the Firebase console and enable Email/Password and Google Sign-in methods.
        b. download `google-services.json` and add it to the app.
2. To enable Google Maps:
    a. Go to APIs & Services at the Google console.
    b. Select your project and go to APIs & Credentials.
    c. Create a new api key and restrict it for android apps.
    d. Add your package name and SHA-1 signing-certificate fingerprint.
    c. Enable Maps SDK for Android from API restrictions and Save.
    d. Copy the api key to the `google_maps_api.xml`
3. Run the app on your mobile phone or emulator with Google Play Services in it.
```

## Testing

Right click on the `test` or `androidTest` packages and select Run Tests

### Break Down Tests

Explain what each test does and why

```
1.androidTest
- at first we test reminder dao this test is small because we are saving one reminder to db ,so we can make sure oue dao work will ,in RemindersDaoTest  we executer rule to  Executes each task synchronously using Architecture Components.
we get the databas and reminder dao and we create the databas      
and allow database on main thread just for testing saveReminder() methode create reminder and save it using dao  and we are geting  saved reminder by id and check if the remider  that saved is the remider that created before using assertThat() finaly we close db.

- in RemindersLocalRepositoryTest this test is Medium test because we using alot of class like databas and rebo and we doing operation on db like save and delete  creating database and allow main thread like before and getReminders we using run blocking to block the thread to run the code immediately create two reminder object add save them to database using repository  and get saved reminder form database using repository.getReminders() and save it in reminders (list)  check if result success and check if reminders not empty list because the list should have two reminder in it 
and in error fun we are using run blocking like befor and create two reminders and save them using Repository after that we delete all reminders form db because we need to check for error in this fun so the result should be error and we cast areminder as result.error so result message = Reminder not found! we check this message using assert true  finaly we close db .

- in ReminderListFragmentTest we are testing reminder list fragment using ReminderDataSource and fragment senario thats provides API to start and drive a Fragment's lifecycle state for testing and we are using Idling to trake whether the app is busy or idle and  we must Unregister your Idling Resource so it can be garbage collected and does not leak any memory, and we are using coin to inject ReminderDataSource and SaveReminderViewModel and dao in setup coin fun but at first we stop      stop koin to remove 'A Koin Application has already been started , in loadReminders_navigate fun get  senario for fragment that we are going to test and start Idling 
using monitorFragment () and pass the senario then we are using mock to navigate and we preform a click on fab button to navigate to add first item in the list but here i do navigation only then verify that navigation is done by check the navController and nav action ReminderListFragmentDirections.toSaveReminder().

in reminderIsShownInRecyclerView() we save reminder and check if reminder is saved into db and show in the recyvlerview we are using repo to add the reminder and create senario for ReminderListFragment and start ideling then we check if the reminder that we are saved is showen in the recucler view by using Espresso.onview 
and pass the reminder and check for it's title and  location and description and chekc using  matches and isDisplayed
finaly in the tearDown () we close coin .

in -utile package we have two class for Idling DataBindingIdlingResource and EspressoIdlingResource

in- RemindersActivityTest it's END TO END test anotation @larg test we are using coin we are using coin to inject ReminderDataSource and SaveReminderViewModel and dao 
and we clear the data to start fresh using  repository.deleteAllReminders() in runblocking scope 
in reminderSaved_ToastMessage we are checking toast message and save reminder to db at first we create senario for RemindersActivity and start idling then 
click on fab button should another fragment apear and click on selected location that's opens the maps then preform long click on the maps after that click save to close the map and save the selected location so after click save we go back and add title then we replacr the edit text add description and title  using ViewActions.replaceText after that click on save reminder then check for message reminder saved ! then check root and activityScenario.close() to close activity 
in clickBack fun () we are testing navigate back  by first get activityScenario click on fab button and add title and description Confirm that if we click back button once, we end up back at the task reminder list page ViewActions.pressBack() after that if addReminderFab is appear so we are in the reminder list page so we check for id addReminderFAB finaly When using ActivityScenario.launch, always call close().
in reminderSavedWithNoSelectedLocation_ToastMessage() fun we are chking for snackbar  at first we start the reminders screen by get the activity that we are testing
after that we using idling like before click on fab button then put text into edittext title  next close the keyboard click on save button  if we don't put the location the snackbar shows  error with please select  location check for snackbar if it has error message Make sure the activity is closed before resetting the db.
in reminderNoTitle it's like with no location but wiht no title .
in remindersScreen_clickOnFab_opensSaveReminderScreen fun () we are chiking navigation by  check that we are on the SaveReminder screen cheking id reminderDescription.
in clear fun  we are using after so after test we clear db usnig delete all reminders and 

2. test
in data package we are creating FakeDataSource  that acts as a test double to the LocalDataSource
 and add list of dto to it's constractor and we are extend ReminderDataSource becouse our RemindersLocalRepository extend that interface too and we are override 
 fun from ReminderDataSource in savereminder fun () we add reminder to the reminders list and it's suspend fun so we must call it from coroutines scope 
  override getReminder () fun thats return result class thats check if result is Success  return  our reminders or error 
  override deleteAllReminders to clear all reminders 
  in RemindersListViewModelTest We Executes each task synchronously using Architecture Components , Set the main coroutines dispatcher for unit testing using MainCoroutineRule and Subject under test is RemindersListViewModel Use a fake repository to be injected into the view model, using fake data source in ListViewModel by inject because it's test double, becouse we don't have lifecycle observer we use live data util (use life observeForever)
  in sandbarError() fun we are fakeDataSource.setReturnError(true) Make the repository return errors so the snack is not null THEN - It's an error, there's a snackBar we are cheking snackbar live data and it's value should be"Test Exception"  and  we are using assertThat to do that check  using hamcrest is for more readability
  in loadReminders_loading fun () we pause the dispatcher to check showLoading live data by  remindersListViewModel.loadReminders() and using assertThatWHEN - the dispatcher is paused, showLoading is true then we call resumeDispatcher() - when the dispatcher is resumed, showboating is false 
  -in SaveReminderViewModelTest et the main coroutines dispatcher for unit testing. Subject under test SaveReminderViewModel and Use a fake repository to be injected into the view model 
  in setupViewModel fun () we call stopKoin()to remove 'A Koin Application has already been started, Initialise the repository with no reminders, get appContext, Initialise saveReminderViewModel and inject fakeDataSource to it .
  in whenIncompleteInfo_validationReturnsTrue() fun we check for validateEnteredData in saveReminderViewModel reminder fields if location and title  not null should return true  we set live data saveReminderViewModel(reminderTitle-reminderDescription-reminderSelectedLocationStr-longitude-latitude)with value and we pass that live data to validateEnteredData thats return boolean that we check if it true by using  MatcherAssert
  in whenIncompleteInfo_validationReturnsNull() fun we do the same but if reminderSelectedLocationStr is null return false and using MatcherAssert to check the boolean value .
  in LiveDataTestUtil.kt we using that class to make observer for our viewmodel 
  in MainCoroutineRule class using to switch to main thread example like if we are using class that do work in background thread but we need that class for testing and we do test in main thread .
  

```

## Project Instructions
    1. Create a Login screen to ask users to login using an email address or a Google account.  Upon successful login, navigate the user to the Reminders screen.   If there is no account, the app should navigate to a Register screen.
    2. Create a Register screen to allow a user to register using an email address or a Google account.
    3. Create a screen that displays the reminders retrieved from local storage. If there are no reminders, display a   "No Data"  indicator.  If there are any errors, display an error message.
    4. Create a screen that shows a map with the user's current location and asks the user to select a point of interest to create a reminder.
    5. Create a screen to add a reminder when a user reaches the selected location.  Each reminder should include
        a. title
        b. description
        c. selected location
    6. Reminder data should be saved to local storage.
    7. For each reminder, create a geofencing request in the background that fires up a notification when the user enters the geofencing area.
    8. Provide testing for the ViewModels, Coroutines and LiveData objects.
    9. Create a FakeDataSource to replace the Data Layer and test the app in isolation.
    10. Use Espresso and Mockito to test each screen of the app:
        a. Test DAO (Data Access Object) and Repository classes.
        b. Add testing for the error messages.
        c. Add End-To-End testing for the Fragments navigation.


## Student Deliverables:

1. APK file of the final project.
2. Git Repository with the code.

## Built With

* [Koin](https://github.com/InsertKoinIO/koin) - A pragmatic lightweight dependency injection framework for Kotlin.
* [FirebaseUI Authentication](https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md) - FirebaseUI provides a drop-in auth solution that handles the UI flows for signing
* [JobIntentService](https://developer.android.com/reference/androidx/core/app/JobIntentService) - Run background service from the background application, Compatible with >= Android O.

## License
