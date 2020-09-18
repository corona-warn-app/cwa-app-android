package de.rki.coronawarnapp.storage

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettings @Inject constructor() {

    val isLast3HourModeEnabled: Boolean
        get() = LocalData.last3HoursMode()
}
