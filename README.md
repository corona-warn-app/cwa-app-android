<h1 align="center">
    Corona Warn App - Android
</h1>

<p align="center">
    <a href="https://github.com/corona-warn-app/cwa-app-android/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-app-android"></a>
    <a href="https://circleci.com/gh/corona-warn-app/cwa-app-android" title="Build Status"><img src="https://circleci.com/gh/corona-warn-app/cwa-app-android.png?circle-token=c26c689ad7833b8c0786752d3e65f56f25f906f3&style=shield"></a>
    <a href="./LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg"></a>
</p>

<p align="center">
  <a href="#development">Development</a> •
  <a href="#architecture--documentation">Documentation</a> •
  <a href="#how-to-contribute">Contribute</a> •
  <a href="#support--feedback">Support</a> •
  <a href="https://github.com/corona-warn-app/cwa-app-android/releases">Changelog</a> •
  <a href="#licensing">Licensing</a>
</p>

The goal of this project is to develop the official Corona-Warn-App for Germany based on the exposure notification API from [Apple](https://www.apple.com/covid19/contacttracing/) and [Google](https://www.google.com/covid19/exposurenotifications/). The apps (for both iOS and Android) use Bluetooth technology to exchange anonymous encrypted data with other mobile phones (on which the app is also installed) in the vicinity of an app user's phone. The data is stored locally on each user's device, preventing authorities or other parties from accessing or controlling the data. This repository contains the **native Android implementation** of the Corona-Warn-App. This implementation is still a **work in progress**, and the code it contains is currently alpha-quality code.

## Development

* [Home](https://github.com/corona-warn-app/cwa-app-android/wiki)
* [1 Setup](https://github.com/corona-warn-app/cwa-app-android/wiki/1-Setup)
* [2 Backend](https://github.com/corona-warn-app/cwa-app-android/wiki/2-Backend)
* [3 UI](https://github.com/corona-warn-app/cwa-app-android/wiki/3-UI)
* [4 Exposure Notification Google API](https://github.com/corona-warn-app/cwa-app-android/wiki/4-Exposure-Notification-Google-API)
* [5 Packages](https://github.com/corona-warn-app/cwa-app-android/wiki/5-Packages)
    
## Known Issues

* The Exposure Notification API is gonna block you from successfully testing the Application unless you are whitelisted inside GMS.
* The Submission Flow is currently not working inside the Test Fragment and is heavily WIP.
* The Exception Handler is heavily WIP and may not include every exception and thus some Toasts (temporary) might not show the error or cause crashes, this also applies to error handling in general.
* The Storage is currently based on Encrypted Shared Preferences and SQL Cipher (SQLite) - this could change in the future
* The App could crash, it is generally heavily WIP. We do not guarantee stability right now.
* Test Coverage is generally low and needs to be improved. We appreciate your help here!
* In General every TODO can be regarded as an issue. You are free to tackle the TODO's anytime!
* We are aware of the Play Store Limitations of GMS.
* Without your own server instance (e.g. a local docker setup), you will not be able to test the Application. For help, please refer to the [server implementation](https://github.com/corona-warn-app/cwa-server).
* Strings in general are WIP, the base language is German, the English translation will be provided by SAP.
* Application design is WIP (e.g. colors, fonts, screen layout) and will be provided once it is final.

## Architecture & Documentation

The full documentation for the Corona-Warn-App is in the [cwa-documentation](https://github.com/corona-warn-app/cwa-documentation) repository. The documentation repository contains technical documents, architecture information, UI/UX specifications, and whitepapers related to this implementation.

## Support & Feedback

The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **General Discussion**   | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="General Discussion"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/question.svg?style=flat-square"></a> </a>   |
| **Concept Feedback**    | <a href="https://github.com/corona-warn-app/cwa-documentation/issues/new/choose" title="Open Concept Feedback"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-documentation/architecture.svg?style=flat-square"></a>  |
| **Android App Issue**    | <a href="https://github.com/corona-warn-app/cwa-app-android/issues/new/choose" title="Open Android Issue"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-app-android?style=flat-square"></a>  |
| **Backend Issue**    | <a href="https://github.com/corona-warn-app/cwa-server/issues/new/choose" title="Open Backend Issue"><img src="https://img.shields.io/github/issues/corona-warn-app/cwa-server?style=flat-square"></a>  |
| **Other Requests**    | <a href="mailto:corona-warn-app.opensource@sap.com" title="Email CWA Team"><img src="https://img.shields.io/badge/email-CWA%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## How to Contribute

Contribution and feedback are encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors

The German government has asked SAP and Deutsche Telekom to develop the Corona-Warn-App for Germany as open source software. Deutsche Telekom is providing the network and mobile technology and will operate and run the backend for the app in a safe, scalable and stable manner. SAP is responsible for the app development, its framework and the underlying platform. Therefore, development teams of SAP and Deutsche Telekom are contributing to this project. At the same time our commitment to open source means that we are enabling -in fact encouraging- all interested parties to contribute and become part of its developer community.

## Repositories

| Repository          | Description                                                           |
| ------------------- | --------------------------------------------------------------------- |
| [cwa-documentation] | Project overview, general documentation, and white papers.            |
| [cwa-app-ios]       | Native iOS app using the Apple/Google exposure notification API.      |
| [cwa-app-android]   | Native Android app using the Apple/Google exposure notification API.  |
| [cwa-server]        | Backend implementation for the Apple/Google exposure notification API.|
| [cwa-verification-server] | Backend implementation of the verification process. |

[cwa-verification-server]: https://github.com/corona-warn-app/cwa-verification-server
[cwa-documentation]: https://github.com/corona-warn-app/cwa-documentation
[cwa-app-ios]: https://github.com/corona-warn-app/cwa-app-ios
[cwa-app-android]: https://github.com/corona-warn-app/cwa-app-android
[cwa-server]: https://github.com/corona-warn-app/cwa-server

## Licensing

Copyright (c) 2020 SAP SE or an SAP affiliate company.

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License. 

You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.
