package de.rki.coronawarnapp.bugreporting.storage

import de.rki.coronawarnapp.bugreporting.event.BugEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBugRepository @Inject constructor() : BugRepository {
    override val newBugEvent: Flow<BugEvent>
        get() = TODO("Not yet implemented")

    override suspend fun getAll(): List<BugEvent> {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: UUID): BugEvent {
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
}
