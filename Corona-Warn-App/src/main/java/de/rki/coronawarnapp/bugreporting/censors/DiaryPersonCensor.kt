package de.rki.coronawarnapp.bugreporting.censors

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.debuglog.DebuggerScope
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.util.CWADebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Reusable
class DiaryPersonCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    diary: ContactDiaryRepository
) : BugCensor {

    private val persons by lazy {
        diary.people.stateIn(
            scope = debugScope,
            started = SharingStarted.Lazily,
            initialValue = null
        ).filterNotNull()
    }

    override suspend fun checkLog(entry: LogLine): LogLine? {
        val personsNow = persons.first()

        if (personsNow.isEmpty()) return null

        var newMessage = personsNow.fold(entry.message) { oldMsg, person ->
            oldMsg.replace(person.fullName, "Person#${person.personId}")
        }

        if (CWADebug.isDeviceForTestersBuild) {
            newMessage = entry.message
        }

        return entry.copy(message = newMessage)
    }
}
