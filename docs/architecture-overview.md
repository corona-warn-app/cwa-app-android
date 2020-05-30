# Libraries 

## Application

### Google Exposure Notification
[Dedicated wiki page](https://github.com/corona-warn-app/cwa-app-android/wiki/4-Google-Exposure-Notifications-API)

### ZXing Embeded
Barcode scanning library by https://journeyapps.com/ based on ZXing decoder.

This library is being used for embedded QR code scanning process during TAN submission to help end users of the application quickly submit their SARS-CoV-2 results without installing additional scanning software.

Licensing: [Apache License 2.0](https://github.com/journeyapps/zxing-android-embedded)

[GitHub](https://github.com/journeyapps/zxing-android-embedded)

### Joda Time
Easy to use standard date and time classes. Used for date calculations, calendar and timezone handling.

Licensing: [Apache License 2.0](https://github.com/JodaOrg/joda-time/blob/master/LICENSE.txt)

[GitHub](https://github.com/JodaOrg/joda-time)  


### Room
Room is a persistence library that provides an abstraction layer over SQLite. In contrary against the EncryptedSharedPreferences Room is used for storing more complex data.

[Documentation](https://developer.android.com/topic/libraries/architecture/room)  

### SQLCipher
Used to encrypt the Room database.

Licensing: [BSD-3](https://github.com/sqlcipher/android-database-sqlcipher/blob/master/SQLCIPHER_LICENSE)

[GitHub](https://github.com/sqlcipher/android-database-sqlcipher)  

## Build

### detekt

detekt is a static code analysis tool for the Kotlin programming language. It operates on the abstract syntax tree provided by the Kotlin compiler. 

Licensing: [Apache License 2.0](https://github.com/detekt/detekt/blob/master/LICENSE)

[GitHub](https://github.com/detekt/detekt)  

### ktlint

Kotlin lint check.

Licensing: [MIT](https://github.com/JLLeitschuh/ktlint-gradle/blob/master/LICENSE.txt)

[GitHub](https://github.com/JLLeitschuh/ktlint-gradle)  

 
# Patterns

## UI architecture

UI architecture follows the MVC pattern which is continuously used throughout the app. Views are separated into Activities, Fragments and Includes whereas an Activity can encorporate multiple Fragments, and Fragments can be built up on multiple Includes. Includes are controllerless helper-views to support reusability of UI components. 

Viewmodels and underlying repositories are used to supply views with data, e.g. provide the current tracing status to the settings view for tracing. Viewmodels are mostly split up on semantic criteria as a viewmodel per fragment with possibly static content is unreasonable. Therefore three main viewmodels are used, supplied with data from multiple repositories.
Repositories are another abstraction layer below viewmodels to move actual data handling out of the UI layer. 

Databinding is the final component to connect the various view types and viewmodels and to enable live updates based upon model data. Whenever pure databinding is insufficient and value change with n-conditions is needed, formatters are used to support this for separation of pure display logic of UI components and more sophisticated features that are done within the view controllers.

# Storage and Encryption

## Database 
The [Room Persistence Library](https://developer.android.com/topic/libraries/architecture/room) is used to store Exposure Summaries retrieved from the Exposure Notification API. These are used to calculate risks levels in accordance to specifications provided by the Robert Koch-Institut. Also we use it as a local persistance library for various complex data structures, e.g. cached date intervals or a map to our downloaded key files. The Room Library uses SQLite by default.

[SQLCipher](https://www.zetetic.net/sqlcipher/) is used to encrypt the database. Thus a key is initialised for the database access the first time we access it. The AppDatabase stores the key inside the shared preferences, which are themselves encrypted and bound to the master key from the android key store. On application reset (in the settings), the complete database is reinitialised. The password is randomly generated and is not used outside the storage package for accessing the data.

Concrete Data Objects:
* KeyCache
* ExposureSummaries
* (inactive) TracingIntervals

# Shared preferences 

[SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences) is used to store the following data:
* application settings
* last calculated risk levels
* time data for tracing
* time data to time background jobs
* time data for calculation of server fetch
* teleTan
* authCode
* registration token
* database password
* flag to check if we can submit diagnosis keys
* the number of successful submissions
* a flag that stores wether notifications are enabled
* the token used for accessing the exposure summaries
* the last time diagnosis keys were retrieved manually
* the last time diagnosis keys were fetched from the server
* the total time tracing was deactivated
* the last time tracing was deactivated
* the first time tracing was activated
* whether the user was onboarded already

For encryption, the [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences) implementation of SharedPreference is used. The EncryptedSharedPreferences encrypt Key-Value Pairs with AES256SIV(Keys) and AES256GCM(Values). The EncryptedSharedPreferences are accessed the same way as the normal Shared Preferences from Android.
This way we make sure everything is accessible only by the android master chain and thus the application.

# Transactions

Atomic locking transactions are implemented using a mutex to reference the co-routine context. A transaction Id is used to to identify the transaction. Execution privilege is first in. 

The general transaction logic is implemented in the abstract Transaction class. The subclasses implement the logic containing the states of the transaction. For details regarding states, please refer to code documentation.

## Retrieve DiagnosisKeys Transaction

Retrieves the diagnosis keys from server and submits them to the Google Exposure Notification API. If successful, updates SharedPreferences persistence with the last retrieval date.

## SubmitDiagnosisKeysTransaction

Submits the diagnosis keys to the server.

## RiskLevelTransaction
Calculates the risk level of the user.