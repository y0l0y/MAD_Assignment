# Club Book Library
## App Description
An android application that use for manage the club's shared collection of books.
The core task is tracking which books are available and which are currently borrowed, along with who borrowed them.
The application also contain the feature on searching online, sorting the book, changing the color theme.

---

## System structure and configuration
1. The file is under the `app/src/main/java/com/example/mad_assignment`
   1. `Model` folder
      1. `dao_and_database` folder
         1. `AppDatabase.kt`: This file is used to build, configure, and initial the SQLite database.
         2. `OfflineDao`: This file is contain the query use to get and set the data into the local database.
      2. `entity` folder
         1. `BookEntity.kt`: This file is to declare the data class of the book entity.
         2. `BookWithLoans.kt`: A separate data class to record the loan status of the book.
         3. `CategoryEntity.kt`: This file is to declare the data class of the category entity.
         4. `CurrentBorrowEntity.kt`: A file that contain two data class that use for local room database.
         5. `LoanEntity.kt`: This file is to declare the data class of the loan entity.
         6. `OfflineBookEntity.kt`: This file is to declare the data class of the offline book entity.
      3. `other` folder
         1. `Book.kt`: This file is used to convert the data from the API to the data class.
         2. `BookQuery.kt`: This file is for three purpose(searching, category filter, and sorting)
         3. `BookRepository.kt`: This file is to combine the OfflineDao function and the Api function together (include firebase).
         4. `BookRules.kt`: This file is to contain the function that use to validate the data entry to the database table.
         5. `CoverStorage.kt`: This file contain the function on convert the picture into JPEG and string and a function about decode the String to JPEG.
         6. `UserPreferencesRepository.kt`: This file is used to contain the function to change the theme of the app and the sorting preference.
      4. `remote` folder
         1. `FirebaseDataSource.kt`: Define how to get and set the data from firebase.
   2. `Network`
      1. `BookApi.kt`: This file is to define the Retrofit Interface on get which data.
      2. `NetworkModule.kt`: This file is the configure and factory for the getting data using the API.
   3. `ui/View`
      1. `AddBookScreen.kt`: This is the file that use to display the UI for the Add book feature.
      2. `ApiBookSearchScreen.kt`: This is the file that contain the function to display the UI for the Book search feature by using the API.
      3. `AppNav.kt`: This is the file that use to define to navigate flow between different UI.
      4. `BookListScreen.kt`: This is the main UI for the app.
      5. `BookUpdateScreen.kt`: This file is to display the UI for update the book info.
      6. `CategoryPicker.kt`: This file is to display the drop-down menu UI for the category selection.
      7. `IsbnScanner.kt`: This file is to define how to call the Google define barcode scanner app and use it.
      8. `LoanHistory.kt`: This file is to define the UI for the book loan history.
      9. `LocalBookSearchScreen.kt`:This is the file that contain the function to display the UI for the local Book search feature.
      10. `MyBooksScreen.kt`: This file is to show which book haven't return and offline book list.
      11. `UiHelpers.kt`: This file is to declare the common function that need to use across different file.
   4. `ViewModel`
      1. `BookViewModel.kt`: This file is the core logic algorithm and state holder for the whole app structure. It links the UI layer and model layer. It uses to process the user interact event and provide the state flow to the UI.
   5. `MainActivity.kt`: This is the main entry of the app

---

## Setup Requirements
- Android Studio Version: Quail 3 | 2026.1.3
- JDK Version: 25
- Minimum SDK: 24
- Target SDK: 37
- Gradle: 9.5.0
- Kotlin: 2.2.10
- Internet connection: for the Firebase and Open Library
- Barcode scanner: require Google Play services

---

## Firebase configuration
This app uses Cloud Firestore and Firebase Authentication in the Firebase project `clublib-88888`.
`app/google-services.json` is included so that the project builds after extraction.
It is the client configuration file, not an admin or service-account key.

The Firebase project must have:
1. **Authentication → Sign-in method → Anonymous:** enabled.
2. Firestore Database: created, with the following rules (published in the *Rules* tab):

```
rules_version = '2';
service cloud.firestore {
   match /databases/{database}/documents {
      function signedIn() { return request.auth != null; }
      
      match /book/{bookId} {
         allow read: if signedIn();
         allow create, update: if signedIn()
            && request.resource.data.title is string
            && request.resource.data.title.size() > 0
            && reuqest.resource.data.title.size() <= 200;
         allow delete: if signedIn();
      }
      match /categories/{categoryId} {
         allow read: if signedIn();
         allow create, update: if signedIn()
            && request.resource.data.name is string
            && request.resource.data.name.size() > 0
            && request.resource.data.name.size() <= 50;
         allow delete: if signedIn();
      }
      match /loans/{loanId} {
         allow read: if signedIn();
         allow create: if signedIn()
            && request.resource.data.borrowerUid == request.auth.id;
         allow update, delete: if signedIn();
      }
   }
 }  
```

To use your own Firebase project instead, create the project, add an Android app 
with the package name `com.example.mad_assignment`, replace `app/google-services.json`, 
and repeat the two steps above.

---

## Build Steps
1. Download the zip file
2. Extract the ZIP
3. Open the project folder in Android Studio
4. Waiting the Gradle sync
5. Use **Build → Make Project** to check

---

## Run Steps
1. Create or select an emulator running API 26 (Android 8.0) or higher 
2. Click Run in Android Studio, then the app will launch in the phone.
3. The book list loads from Firestore. If it cannot connect, the screen shows an error message and a Retry button.

---


## How to demonstrate the network and device features
**Network (Open Library)**
1. **Menu → Search Book Online**: type in the book title and tap search. The screen shows a loading spinner when getting the data from the Open Library, then show the result of searching.
2. **Empty state**: search for a nonsense word such as `xtfrgb`.
3. **Error and Retry**: turn on aeroplane mode, search the book, then turn the network on (aeroplane mode off) and tap Retry.
4. **Bad data**: result with no title are skipped, a missing author is shown as "Unknown author", and repeated title/author pairs appear once.

**Device feature (in Admin Mode, tap + to add book)**
1. **Gallery**: tap the cover area and choose an image. Press Back in the picker to cancel
2. **Barcode Scanner**: tap **Scan ISBN barcode** and scan the EAN-13 barcode on the back of the book.
The ISBN, title, author and cover are filled in from Open Library. 
If the lookup fails or the network is off, a message is shown and the scanned ISBN is kept.
Closing the scanner cancels it. If the scanner si not available, a message asks the user to type the ISBN.

**Offline use**: open the library once with internet, turn on aeroplane mode, then open **Menu -> My Books**.
The **Saved Offline** tab lists the library from Room.
Borrowing or adding a book while offline shows a connection message and does not crash.

