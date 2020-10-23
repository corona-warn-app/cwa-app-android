package de.rki.coronawarnapp.bugreporting.storage

import de.rki.coronawarnapp.bugreporting.event.BugEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface BugRepository {

    val newBugEvent: Flow<BugEvent>

    suspend fun getAll(): List<BugEvent>

    suspend fun get(id: UUID): BugEvent

    suspend fun save(bugEvent: BugEvent)

    suspend fun clear()
}
