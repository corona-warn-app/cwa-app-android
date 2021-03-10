package de.rki.coronawarnapp.bugreporting.censors

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
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

        val newMessage = locationsNow.fold(entry.message) { orig, location ->
            var wip = orig.replace(location.locationName, "Location#${location.locationId}/Name")

            location.emailAddress?.let {
                wip = wip.replace(it, "Location#${location.locationId}/EMail")
            }

            location.phoneNumber?.let {
                wip = wip.replace(it, "Location#${location.locationId}/PhoneNumber")
            }

            wip
        }

        return entry.copy(message = newMessage)
    }
}
