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

        val newMessage = personsNow.fold(entry.message) { oldMsg, person ->
            if (CWADebug.isDeviceForTestersBuild) {
                // We want this info in tester builds, but we also want to know censoring is working
                oldMsg.replace(person.fullName, "${person.fullName}#${person.personId}")
            } else {
                oldMsg.replace(person.fullName, "Person#${person.personId}")
            }
        }

        return entry.copy(message = newMessage)
    }
}
