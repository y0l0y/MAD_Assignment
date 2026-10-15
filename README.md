# Club Book Library

## App Description

An Android application for managing the club's shared collection of books.
The core task is tracking which books are available and which are currently borrowed, and by whom.

* **Browse without an account.** The book list ships inside the app and works offline from the first launch.
* **Borrow and return with an account.** Students sign in with email and password, borrow a book, and later request its return. An admin (committee member) confirms the return.
* **Admin tools.** Admins add, edit and delete books and categories, see the queue of returns waiting for confirmation, and see every loan with overdue loans highlighted.
* **Extras.** Search the book list (title, author, ISBN), filter by category, sort, search Open Library online, scan an ISBN barcode, and switch the colour theme.

---

## How it works

|Part|Where it lives|Needs internet|
|-|-|-|
|Book list and categories|Room database on the phone. Seeded from `assets/catalog\_seed.json` on first launch.|No|
|Catalog updates|Cloud Firestore `books` and `categories`. A version number in `meta/catalog` tells the phone when to download again.|Only to sync|
|Who holds each book|Firestore `bookStatus/{bookId}` (small documents, no personal data)|Yes|
|Loans (borrow, request return, confirm return)|Firestore `loans/{loanId}`, written by transactions|Yes, and signed in|
|Accounts and roles|Firebase Authentication (email and password) and `users/{uid}` (`name`, `email`, `role`)|Yes|

**Roles.** Every new account is a `student`. An admin has `role = "admin"` in their `users` document, which is changed in the Firebase console. Security rules (`firestore.rules`) enforce all of this on the server, so the app UI is not the only protection.

**Loan life cycle.** `Borrowed` -> the borrower taps *Request return* -> `Return requested` -> an admin taps *Confirm return* -> `Returned`. A book becomes available again only after the admin confirms.

**Privacy.** Students only read their own loans and cannot see who borrowed a book. Admins see everything.

---

## System structure and configuration

1. The code is under `app/src/main/java/com/example/mad_assignment`

   1. `Model` folder

      1. `dao_and_database` folder

         1. `AppDatabase.kt`: Builds and configures the Room (SQLite) database.
         2. `CatalogDao.kt`: Queries for the local book list, categories and catalog version.
      2. `entity` folder

         1. `BookEntity.kt`: The book data class (Firestore and UI).
         2. `BookStatus.kt`: The `bookStatus` document (which loan holds a book).
         3. `BookWithLoans.kt`: A book together with its loans and availability.
         4. `CatalogEntities.kt`: The Room tables for the local book list, and the mapping functions.
         5. `CategoryEntity.kt`: The category data class.
         6. `LoanEntity.kt`: The loan data class (borrower, dates, return request).
         7. `UserProfile.kt`: The `users/{uid}` document and the role names.
      3. `other` folder

         1. `Book.kt`: Converts the Open Library API data into a data class.
         2. `BookQuery.kt`: Text search, category filter and sorting.
         3. `BookRepository.kt`: Combines the local catalog, Firebase and the API; also does the catalog version sync.
         4. `BookRules.kt`: Validation of book, category, email, password and name input.
         5. `CatalogSeed.kt`: Reads the bundled `assets/catalog_seed.json` (skips invalid rows).
         6. `CoverStorage.kt`: Converts a cover picture to a JPEG string and back.
         7. `NetworkMonitor.kt`: Reports whether the phone has internet.
         8. `UserPreferencesRepository.kt`: Stores the theme and sort preference.
      4. `remote` folder

         1. `FirebaseDataSource.kt`: All Firebase calls: sign in, catalog, status, loan transactions.
   2. `Network`

      1. `BookApi.kt`: The Retrofit interface for Open Library.
      2. `NetworkModule.kt`: Configuration and factory for the API calls.
   3. `ui/View`

      1. `AddBookScreen.kt`: Add a book (admin).
      2. `AdminLoansScreen.kt`: Pending returns and the all-loans table (admin).
      3. `ApiBookSearchScreen.kt`: Search books online with Open Library.
      4. `AppNav.kt`: The navigation between screens.
      5. `AuthScreen.kt`: Sign in and create account.
      6. `BookListScreen.kt`: The main screen.
      7. `BookUpdateScreen.kt`: Edit a book (admin).
      8. `CategoryPicker.kt`: The category drop-down menu.
      9. `IsbnScanner.kt`: Calls Google's barcode scanner.
      10. `LoanHistory.kt`: The loan history of one book (admin).
      11. `LocalBookSearchScreen.kt`: Search, filter and sort the local book list.
      12. `MyBooksScreen.kt`: A student's unreturned books (admins see the loans screen instead).
      13. `OfflineBanner.kt`: The "you are offline" notice.
      14. `UiHelpers.kt`: Shared composables and helpers.
   4. `ViewModel`

      1. `BookViewModel.kt`: The state holder and logic that connects the UI and the model.
   5. `MainActivity.kt`: The entry point of the app.
2. `app/src/main/assets/catalog_seed.json`: The book list that ships with the app.
3. `firestore.rules`: The Firestore security rules.

---

## Setup Requirements

* Android Studio Version: Quail 3 | 2026.1.3
* JDK Version: 25
* Minimum SDK: 24
* Target SDK: 37
* Gradle: 9.5.0
* Kotlin: 2.2.10
* Internet connection: for sign in, borrowing, Firebase sync and Open Library
* Barcode scanner: requires Google Play services

---

## Firebase configuration

`app/google-services.json` is included so that the project builds after extraction.
It is the client configuration file, not an admin or service-account key.

To use your own Firebase project, create it, add an Android app with the package name
`com.example.mad_assignment`, replace `app/google-services.json`, and then:

1. **Authentication -> Sign-in method -> Email/Password:** enable it. (Anonymous sign-in is not used.)
2. **Firestore Database:** create it, then open the *Rules* tab, paste the content of `firestore.rules` and publish.
3. **First admin:** run the app, create an account, then in the Firestore console open `users/<that account's uid>` and change `role` from `student` to `admin`. Restart the app.
4. **Publish the catalog:** the first time an admin is signed in with no catalog online, the app uploads the bundled book list (`books`, `bookStatus`, `categories` and `meta/catalog`). Other phones then follow the version number.

Collections:

* `users/{uid}`: `name`, `email`, `role`
* `books/{id}`, `categories/{id}`, `meta/catalog` (`version`)
* `bookStatus/{bookId}`: `activeLoanId` (null = on the shelf)
* `loans/{id}`: `bookId`, `bookTitle`, `borrowerName`, `borrowerUid`, `borrowedAt`, `dueAt`, `returnRequestedAt`, `returnedAt`

### Changing the starting book list

Edit `app/src/main/assets/catalog_seed.json` (`{"version": 1, "categories": \[...], "books": \[{"title", "author", "isbn", "category"}]}`).
The bundled list is used on first launch. Later changes to the catalog are made by an admin inside the app.

---

## Build Steps

1. Download the zip file
2. Extract the ZIP
3. Open the project folder in Android Studio
4. Waiting the Gradle sync
5. Use **Build -> Make Project** to check

---

## Run Steps

1. Create or select an emulator running API 26 (Android 8.0) or higher
2. Click Run in Android Studio, then the app will launch in the phone.
3. The book list appears immediately from the phone. Availability appears once the app reaches Firebase.

---

## How to demonstrate the features

**Accounts and roles**

1. Open the app: the list works as a guest. Tap **Borrow** button, and the sign-in screen opens.
2. **Menu -> Sign in / Create account**: create a student account and borrow a book.
3. Sign in as an admin (see *Firebase configuration*): the menu shows "(admin)", the add button and the edit menu appear, and **Menu -> Loans (admin)** opens the loans screen.

**Return flow**

1. As a student, tap **Request return** on the book (list or **Menu -> My Books**). It shows "Return requested".
2. As an admin, open **Menu -> Loans (admin)**. The loan is under *Pending returns*. Tap **Confirm return**. The book is available again.
3. *All loans* lists every loan. Overdue loans are highlighted and can be filtered.

**Network (Open Library)**

1. **Menu -> Search Book Online**: type in the book title and tap search. The screen shows a loading spinner when getting the data from the Open Library, then shows the result of searching.
2. **Empty state**: search for a nonsense word such as `xtfrgb`.
3. **Error and Retry**: turn on aeroplane mode, search the book, then turn the network on (aeroplane mode off) and tap Retry.
4. **Bad data**: results with no title are skipped, a missing author is shown as "Unknown author", and repeated title/author pairs appear once.

**Device features (signed in as admin, tap + to add a book)**

1. **Gallery**: tap the cover area and choose an image. Press Back in the picker to cancel.
2. **Barcode Scanner**: tap **Scan ISBN barcode** and scan the EAN-13 barcode on the back of the book.
The ISBN, title, author and cover are filled in from Open Library.
If the lookup fails or the network is off, a message is shown and the scanned ISBN is kept.
Closing the scanner cancels it. If the scanner is not available, a message asks the user to type the ISBN.

**Offline use**: turn on aeroplane mode and open the app. The whole book list, search, filter and sorting still work.
A banner says you are offline, and borrowing, requesting or confirming a return shows a message instead (these need internet).

