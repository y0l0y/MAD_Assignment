# Club Book Library
## App Description
This is a kotlin application that use for manage the club's shared collection of books.
The core task is tracking which books are available and which are currently borrowed, along with who borrowed them.
The application also contain the feature on searching online, sorting the book, changing the color theme.

## System structure and configuration
1. The file is under the `app/src/main/java/com/example/mad_assignment`
    1. `Model`
        1. `AppDatabase.kt`: This file is use to build, configure, and initial the SQLite database.
        2. `Book.kt`: This file is use to convert the data from the API to the data class.
        3. `BookEntity.kt`: This file is to declare the structure of the database table.
        4. `BookRepository.kt`: This file is to combine the Bookdao function and the Api function together.
        5. `BookRules.kt`: This file is to contain the function that use to validate the data entry to the database table.
        6. `BooksDao.kt`: This file is contain the query use to get and set the data into the database.
        7. `CoverStorage.kt`: This file is to save the book cover picture into fileDir and return the absolute file path.
        8. `UserPreferencesRepository.kt`: This file is use to contain the function to change the theme of the app.
    2. `Network`
        1. `BookApi.kt`: This file is to define the Retrofit Interface.
        2. `NetworkModule.kt`: This file is the configure and factory for the getting data using the API.
    3. `ui/View`
        1. `AddBookScreen.kt`: This is the file that use to display the UI for the Add book feature.
        2. `ApiBookSearchScreen.kt`: This is the file that contain the function to display the UI for the Book search feature by using the API.
        3. `AppNav.kt`: This is the file that use to define the navigate between different UI.
        4. `BookListScreen.kt`: This is the main UI for the app.
        5. `BookUpdateScreen.kt`: This file is to display the UI for update the book info.
        6. `LocalBookSearchScreen.kt`:This is the file that contain the function to display the UI for the local Book search feature.
    4. `ViewModel`
        1. `BookViewModel.kt`: This file is the core logic algorithm and state holder for the whole app structure. It link the UI layer and model layer. It use to process the user interact event and provide the state flow to the UI.
    5. `MainActivity.kt`: This is the main entry of the app

## Setup Requirements
- Android Studio Version: Quail 3 | 2026.1.3
- JDK Version: 25
- Minimum SDK: 24
- Target SDK: 37

## Build Steps
1. Download the zip file
2. Extract the ZIP
3. Open the project folder in Android Studio
4. Waiting the Gradle sync

## Run Steps
1. Create or select an emulator running API 26 (Android 8.0) or higher 
2. Click Run in Android Studio, then the app will launch in the phone.

