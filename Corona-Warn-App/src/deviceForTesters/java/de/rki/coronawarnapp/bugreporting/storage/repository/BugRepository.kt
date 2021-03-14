package de.rki.coronawarnapp.bugreporting.storage.repository

import de.rki.coronawarnapp.bugreporting.event.BugEvent
import kotlinx.coroutines.flow.Flow

interface BugRepository {
    fun getAll(): Flow<List<BugEvent>>
    fun get(id: Long): Flow<BugEvent>
    suspend fun save(bugEvent: BugEvent)
    suspend fun clear()
}
