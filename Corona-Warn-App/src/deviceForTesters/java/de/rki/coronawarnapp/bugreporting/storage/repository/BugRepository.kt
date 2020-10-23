package de.rki.coronawarnapp.bugreporting.storage.repository

import androidx.lifecycle.LiveData
import de.rki.coronawarnapp.bugreporting.event.BugEvent

interface BugRepository {
    fun getAll(): LiveData<List<BugEvent>>
    fun get(id: Long): LiveData<BugEvent>
    suspend fun save(bugEvent: BugEvent)
    suspend fun clear()
}
