package de.rki.coronawarnapp.bugreporting.censors.contactdiary

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensoredString
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.censor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.plus
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNullIfUnmodified
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class DiaryVisitCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    diary: ContactDiaryRepository
) : BugCensor {

    private val mutex = Mutex()

    private val visitsHistory = mutableSetOf<ContactDiaryLocationVisit>()

    init {
        diary.locationVisits
            .onEach { locationVisitList ->
                val visitsWithCircumstances = locationVisitList.filterNot { it.circumstances.isNullOrBlank() }
                mutex.withLock {
                    visitsHistory.addAll(visitsWithCircumstances)
                }
            }
            .launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensoredString? = mutex.withLock {

        if (visitsHistory.isEmpty()) return null

        val newMessage = visitsHistory.fold(CensoredString(message)) { orig, visit ->
            var wip = orig

            BugCensor.withValidComment(visit.circumstances) {
                wip += wip.censor(it, "Visit#${visit.id}/Circumstances")
            }

            wip
        }

        return newMessage.toNullIfUnmodified()
    }
}
