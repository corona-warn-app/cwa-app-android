package de.rki.coronawarnapp.bugreporting.storage.dao

import de.rki.coronawarnapp.bugreporting.event.BugEvent
import kotlinx.coroutines.flow.Flow

interface BugDao<T : BugEvent> {
    suspend fun insertBugEvent(bugEvent: T)
    fun getBugEvent(id: Long): Flow<T>
    fun getAllBugEvents(): Flow<List<T>>
    suspend fun deleteBugEvent(id: Long)
    suspend fun deleteAllBugEvents()
}
