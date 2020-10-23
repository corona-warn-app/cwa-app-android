package de.rki.coronawarnapp.bugreporting.storage.repository

import de.rki.coronawarnapp.bugreporting.event.BugEvent
import de.rki.coronawarnapp.bugreporting.event.BugEventEntity
import de.rki.coronawarnapp.bugreporting.storage.repository.BugRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBugRepository @Inject constructor() : BugRepository {
    override val newBugEvent: Flow<BugEvent>
        get() = TODO("Not yet implemented")

    override suspend fun getAll(): List<BugEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: Long): BugEvent {
        TODO("Not yet implemented")
    }

    override suspend fun save(bugEvent: BugEvent) {
        // TODO Map the interface to an actual storage object
//        val converted = bugEvent.toStoredType()
        TODO("Not yet implemented")
    }

    override suspend fun clear() {
        TODO("Not yet implemented")
    }

    private fun BugEvent.toStoredType(): BugEventEntity {
        TODO("add values from interface to our internal dao entity?")
    }
}
