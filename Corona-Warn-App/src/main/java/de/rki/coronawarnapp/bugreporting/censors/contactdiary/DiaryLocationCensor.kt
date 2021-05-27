package de.rki.coronawarnapp.bugreporting.censors.contactdiary

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensoredString
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.censor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.plus
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNullIfUnmodified
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidEmail
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidPhoneNumber
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class DiaryLocationCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    diary: ContactDiaryRepository
) : BugCensor {

    private val mutex = Mutex()

    private var locationHistory = mutableSetOf<ContactDiaryLocation>()

    init {
        diary.locations
            .onEach { mutex.withLock { locationHistory.addAll(it) } }
            .launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensoredString? = mutex.withLock {

        if (locationHistory.isEmpty()) return null

        val newMessage = locationHistory.fold(CensoredString(message)) { orig, location ->
            var wip = orig

            withValidName(location.locationName) {
                wip += wip.censor(it, "Location#${location.locationId}/Name")
            }
            withValidEmail(location.emailAddress) {
                wip += wip.censor(it, "Location#${location.locationId}/EMail")
            }
            withValidPhoneNumber(location.phoneNumber) {
                wip += wip.censor(it, "Location#${location.locationId}/PhoneNumber")
            }

            wip
        }

        return newMessage.toNullIfUnmodified()
    }
}
