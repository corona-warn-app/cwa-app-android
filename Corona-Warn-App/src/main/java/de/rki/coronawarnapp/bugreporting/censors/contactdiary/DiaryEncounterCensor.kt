package de.rki.coronawarnapp.bugreporting.censors.contactdiary

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensoredString
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.censor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.plus
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNullIfUnmodified
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidComment
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class DiaryEncounterCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    diary: ContactDiaryRepository
) : BugCensor {

    // We keep a history of all encounters so that we can censor them even after they got deleted
    private val encounterHistory = mutableSetOf<ContactDiaryPersonEncounter>()

    val mutex = Mutex()

    init {
        diary.personEncounters
            .onEach { mutex.withLock { encounterHistory.addAll(it) } }
            .launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensoredString? = mutex.withLock {

        if (encounterHistory.isEmpty()) return null

        val newMessage = encounterHistory.fold(CensoredString.fromOriginal(message)) { orig, encounter ->
            var wip = orig

            withValidComment(encounter.circumstances) {
                wip += wip.censor(it, "Encounter#${encounter.id}/Circumstances")
            }

            wip
        }

        return newMessage.toNullIfUnmodified()
    }
}
