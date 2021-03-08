package de.rki.coronawarnapp.bugreporting.censors

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.util.CWADebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Reusable
class DiaryLocationCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    diary: ContactDiaryRepository
) : BugCensor {

    private val locations by lazy {
        diary.locations.stateIn(
            scope = debugScope,
            started = SharingStarted.Lazily,
            initialValue = null
        ).filterNotNull()
    }

    override suspend fun checkLog(entry: LogLine): LogLine? {
        val locationsNow = locations.first()

        if (locationsNow.isEmpty()) return null

        val newMessage = locationsNow.fold(entry.message) { oldMsg, location ->
            if (CWADebug.isDeviceForTestersBuild) {
                // We want this info in tester builds, but we also want to know censoring is working
                oldMsg.replace(location.locationName, "${location.locationName}#${location.locationId}")
            } else {
                oldMsg.replace(location.locationName, "Location#${location.locationId}")
            }
        }

        return entry.copy(message = newMessage)
    }
}
