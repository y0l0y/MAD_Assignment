# Club Library (Club Book Library)

COMP2008 Mobile Application Development, Part A.

An Android app (Kotlin, Jetpack Compose, MVVM) for managing a club's shared collection of books: which books are available, who holds each one and when it is due.

> The four-point app description (users, core task, data, out of scope) is in **`README_AppDescription.md`**. This file covers setup, build, run and demonstration steps.

---

## What the app does

* **Browse without an account.** The book list ships inside the app and works offline from the first launch.
* **Borrow and return with an account.** Borrowers sign in with email and password, borrow a book (14-day due date), and later request its return. An admin (committee member) confirms the return.
* **Admin tools.** Admins add, edit and delete books and categories, see the returns waiting for confirmation, and see every loan with overdue loans highlighted.
* **Extras.** Search the list (title, author, ISBN), filter by category, sort, search Open Library online, scan an ISBN barcode, and switch between light and dark theme.

## How it works

| Part | Where it lives | Needs internet |
| --- | --- | --- |
| Book list and categories | Room database on the phone, seeded from `assets/catalog_seed.json` on first launch | No |
| Catalogue updates | Cloud Firestore `books` and `categories`. A version number in `meta/catalog` tells the phone when to download again | Only to sync |
| Who holds each book | Firestore `bookStatus/{bookId}` (small documents, no personal data) | Yes |
| Loans (borrow, request return, confirm return) | Firestore `loans/{loanId}`, written by transactions | Yes, and signed in |
| Accounts and roles | Firebase Authentication (email and password) and `users/{uid}` (`name`, `email`, `role`) | Yes |
| Theme and sort order | Jetpack DataStore on the phone | No |
| Online book search and ISBN lookup | Open Library API (Retrofit + coroutines) | Yes |

**Roles.** Every new account is a `student`. An admin has `role = "admin"` in their `users` document, which is changed in the Firebase console. The security rules (`firestore.rules`) enforce this on the server, so the app UI is not the only protection.

**Loan life cycle.** `Borrowed` → the borrower taps *Request return* → `Return requested` → an admin taps *Confirm return* → `Returned`. A book becomes available again only after the admin confirms.

**Privacy.** Students read only their own loans and cannot see who borrowed a book. Admins see everything.

**Offline.** Browsing, search, filter and sort work offline. Borrowing, returning and changing the catalogue need internet, and the app shows a message instead of failing.

---

## Setup requirements

* Android Studio: Quail 3 | 2026.1.3
* JDK: 25 (the Gradle daemon toolchain is set to 25; Android Studio's bundled JDK is used if it matches, otherwise Gradle downloads it, so the first sync needs internet)
* Gradle: 9.5.0 (downloaded by the Gradle wrapper)
* Android Gradle Plugin: 9.3.3
* Kotlin: 2.2.10
* Minimum SDK: 24, Target / compile SDK: 37 (install the **Android 37 SDK Platform** in the SDK Manager)
* Internet connection: needed for the first Gradle sync, and for sign in, borrowing, Firebase sync and Open Library
* Barcode scanner: needs Google Play services, so use an emulator image **with Google Play** or a physical phone

---

## Build steps

1. Extract the ZIP.
2. Open the extracted project folder in Android Studio.
3. Wait for the Gradle sync to finish.
4. Use **Build → Make Project** to check that it builds.

(Command line: `./gradlew assembleDebug`, or `gradlew.bat assembleDebug` on Windows.)

## Run steps

1. Create or select an emulator running Android 8.0 (API 26) or higher with Google Play, or connect a phone.
2. Click **Run** in Android Studio. The app launches on the device.
3. The book list appears immediately from the phone. Availability appears once the app reaches Firebase (until then each book shows "Checking…").

---

## How to demonstrate the features

The app uses the group's Firebase project (`app/google-services.json`). No passwords are stored in the repository. Create a student account in the app. To get an admin account, see *First admin* under *Firebase configuration*.

### Accounts, roles and the core task

1. Open the app as a guest. The list works. Tap **Borrow** and the sign-in screen opens.
2. **Menu → Sign in / Create account:** create a student account and borrow a book. Its chip changes to "Your loan" with a due date.
3. Sign in as an admin: the menu shows "(admin)", the **+** button and the edit menu appear, and **Menu → Loans (admin)** opens the loans screen.

**Return flow**

1. As a student, tap **Request return** on the book (list or **Menu → My Books**). It shows "Return requested".
2. As an admin, open **Menu → Loans (admin)**. The loan is under *Pending returns*. Tap **Confirm return**. The book is available again.
3. *All loans* lists every loan. Overdue loans are highlighted and can be filtered.

### Network (Open Library) — signed in as admin

Tap **+** to open *Add book*, then tap **Search book online**.

1. **Loading and success:** type a title and tap **Search**. A spinner shows while the data loads, then the results. Tap a result to fill the add-book form.
2. **Empty state:** search for a nonsense word such as `xtfrgb` (the request succeeds with no results).
3. **Error and Retry:** turn on aeroplane mode, search, then turn aeroplane mode off and tap **Retry**.
4. **Bad data:** results with no title are skipped, a missing author is shown as "Unknown author", and repeated title/author pairs appear once.

### Device and inter-app features — signed in as admin, **+** (Add book)

1. **Gallery:** tap the cover area and choose an image. Press Back in the picker to cancel; the form is unchanged. (The picker needs no storage permission.)
2. **Barcode scanner:** tap **Scan ISBN barcode** and scan the EAN-13 barcode on the back of a book. The ISBN, title, author and cover are filled in from Open Library. If the lookup fails or the network is off, a message is shown and the scanned ISBN is kept. Closing the scanner cancels it. If the scanner is not available, a message asks the user to type the ISBN. (The Google code scanner needs no camera permission from the app.)

### Offline use

Turn on aeroplane mode and open the app. The whole book list, search, filter and sorting still work. A banner says you are offline. Borrowing, requesting or confirming a return, and admin catalogue changes show a message instead (they need internet).

### Rotation, screen size and text size

* **Rotation:** open *Add book*, type a title and author, then rotate the device. The values stay. Search text, the sign-in form and the selected loan filter also survive rotation.
* **Landscape and tablet:** use a phone in landscape or a tablet / resizable emulator. At 600 dp wide or more the screens switch to wider layouts (more columns, width-limited content).
* **Larger text:** Settings → Display → Font size / Display size, set to the largest. Text is in `sp` and screens scroll.
* **Theme and sort (DataStore):** **Menu → Dark Mode**, and change the sort chips on the list. Close and reopen the app and both are remembered.

---

## Firebase configuration

`app/google-services.json` is included so the project builds after extraction. It is the client configuration file, not an admin or service-account key.

To use your own Firebase project, create it, add an Android app with the package name `com.example.mad_assignment`, replace `app/google-services.json`, and then:

1. **Authentication → Sign-in method → Email/Password:** enable it. (Anonymous sign-in is not used.)
2. **Firestore Database:** create it, open the *Rules* tab, paste the content of `firestore.rules` and publish.
3. **First admin:** run the app, create an account, then in the Firestore console open `users/<that account's uid>` and change `role` from `student` to `admin`. Restart the app.
4. **Publish the catalogue:** the first time an admin is signed in and no catalogue is online, the app uploads the bundled book list (`books`, `bookStatus`, `categories` and `meta/catalog`). Other phones then follow the version number.

Collections:

* `users/{uid}`: `name`, `email`, `role`
* `books/{id}`, `categories/{id}`, `meta/catalog` (`version`)
* `bookStatus/{bookId}`: `activeLoanId` (null = on the shelf)
* `loans/{id}`: `bookId`, `bookTitle`, `borrowerName`, `borrowerUid`, `borrowedAt`, `dueAt`, `returnRequestedAt`, `returnedAt`

### Changing the starting book list

Edit `app/src/main/assets/catalog_seed.json`:
`{"version": 1, "categories": [...], "books": [{"title", "author", "isbn", "category"}]}`.
The bundled list is used on first launch (rows with a missing title, author or ISBN, or a repeated ISBN, are skipped). Later changes to the catalogue are made by an admin inside the app.

---

## Project structure

Code is under `app/src/main/java/com/example/mad_assignment`.

1. `model/`
   1. `dao_and_database/`
      * `AppDatabase.kt`: builds and configures the Room (SQLite) database.
      * `CatalogDao.kt`: queries for the local book list, categories and catalogue version, and the transaction that replaces the catalogue.
   2. `entity/`
      * `BookEntity.kt`: the book data class (Firestore and UI).
      * `BookStatus.kt`: the `bookStatus` document (which loan holds a book).
      * `BookWithLoans.kt`: a book together with its loans and availability.
      * `CatalogEntities.kt`: the Room tables (`catalog_books`, `catalog_categories`, `catalog_meta`), the foreign key and relation classes, and the mapping functions.
      * `CategoryEntity.kt`: the category data class.
      * `LoanEntity.kt`: the loan data class (borrower, dates, return request).
      * `UserProfile.kt`: the `users/{uid}` document and the role names.
   3. `other/`
      * `Book.kt`: converts Open Library API data into a data class and cleans it.
      * `BookQuery.kt`: text search, category filter and sorting.
      * `BookRepository.kt`: combines the local catalogue, Firebase and the API; also does the catalogue version sync.
      * `BookRules.kt`: validation of book, category, email, password and name input, and the overdue rule.
      * `CatalogSeed.kt`: reads the bundled `assets/catalog_seed.json` (skips invalid rows).
      * `CoverStorage.kt`: converts a cover picture to a JPEG string and back.
      * `LoanQuery.kt`: loan status, overdue and filtering for the admin screens.
      * `NetworkMonitor.kt`: reports whether the phone has internet.
      * `UserPreferencesRepository.kt`: stores the theme and sort preference in DataStore.
   4. `remote/`
      * `FirebaseDataSource.kt`: all Firebase calls: sign in, catalogue, status, loan transactions.
2. `network/`
   * `BookApi.kt`: the Retrofit interface for Open Library.
   * `NetworkModule.kt`: configuration and factory for the API calls.
3. `ui/`
   * `view/`
     * `AddBookScreen.kt`: add a book (admin).
     * `AdminLoansScreen.kt`: pending returns and the all-loans list (admin).
     * `ApiBookSearchScreen.kt`: search books online with Open Library.
     * `AppNav.kt`: navigation between screens (seven routes).
     * `AuthScreen.kt`: sign in and create account.
     * `BookListScreen.kt`: the main screen (search, filter, sort, side menu).
     * `BookUpdateScreen.kt`: book detail and edit (admin), opened with the book ID.
     * `CategoryPicker.kt`: the category drop-down menu.
     * `IsbnScanner.kt`: calls Google's barcode scanner.
     * `LoanHistory.kt`: the loan history of one book (admin), opened with the book ID.
     * `MyBooksScreen.kt`: a student's unreturned books (admins see the loans screen instead).
     * `OfflineBanner.kt`: the "you are offline" notice.
     * `UiHelpers.kt`: shared composables and helpers.
   * `components/FolioComponents.kt`: shared Material 3 building blocks and the wide-screen check.
   * `theme/`: colours, typography and the Material 3 theme.
4. `viewModel/BookViewModel.kt`: the state holder and logic that connects the UI and the model.
5. `MainActivity.kt`: the entry point of the app.

Other files: `app/src/main/assets/catalog_seed.json` (the book list that ships with the app) and `firestore.rules` (the Firestore security rules).