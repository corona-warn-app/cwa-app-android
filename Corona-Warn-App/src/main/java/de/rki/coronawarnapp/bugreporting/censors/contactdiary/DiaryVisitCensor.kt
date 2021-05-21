package de.rki.coronawarnapp.bugreporting.censors.contactdiary

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensoredString
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.censor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.plus
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNullIfUnmodified
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Reusable
class DiaryVisitCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    diary: ContactDiaryRepository
) : BugCensor {

    private val visits by lazy {
        diary.locationVisits.stateIn(
            scope = debugScope,
            started = SharingStarted.Lazily,
            initialValue = null
        ).filterNotNull()
    }

    override suspend fun checkLog(message: String): CensoredString? {
        val visitsNow = visits.first().filter { !it.circumstances.isNullOrBlank() }

        if (visitsNow.isEmpty()) return null

        val newMessage = visitsNow.fold(CensoredString(message)) { orig, visit ->
            var wip = orig

            BugCensor.withValidComment(visit.circumstances) {
                wip += wip.censor(it, "Visit#${visit.id}/Circumstances")
            }

            wip
        }

        return newMessage.toNullIfUnmodified()
    }
}
