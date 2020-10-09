package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.util.CWADebug
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettings @Inject constructor() {

    val isHourlyTestingMode: Boolean
        get() = LocalData.isHourlyTestingMode && CWADebug.isDebugBuildOrMode
}
