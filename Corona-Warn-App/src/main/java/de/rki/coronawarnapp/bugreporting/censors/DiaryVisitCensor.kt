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

    override suspend fun checkLog(entry: LogLine): LogLine? {
        val visitsNow = visits.first().filter { !it.circumstances.isNullOrBlank() }

        if (visitsNow.isEmpty()) return null

        val newMessage = visitsNow.fold(entry.message) { orig, visit ->
            if (visit.circumstances.isNullOrBlank()) return@fold orig

            orig.replace(visit.circumstances!!, "Visit#${visit.id}/Circumstances")
        }

        return entry.copy(message = newMessage)
    }
}
