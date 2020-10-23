package de.rki.coronawarnapp.bugreporting.storage.dao

import androidx.lifecycle.LiveData
import de.rki.coronawarnapp.bugreporting.event.BugEvent

interface BugDao<T : BugEvent> {
    suspend fun insertBugEvent(bugEvent: T)
    fun findBugEvent(id: Long): LiveData<T>
    fun getAllBugEvents(): LiveData<List<T>>
    suspend fun deleteBugEvent(id: Long)
    suspend fun deleteAllBugEvents()
}
