package de.rki.coronawarnapp.bugreporting.storage.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import de.rki.coronawarnapp.bugreporting.event.BugEvent
import de.rki.coronawarnapp.bugreporting.event.BugEventEntity
import de.rki.coronawarnapp.bugreporting.storage.dao.BugDao
import de.rki.coronawarnapp.bugreporting.storage.dao.DefaultBugDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBugRepository @Inject constructor(
    private val bugDao: DefaultBugDao
) : BugRepository {

    override fun getAll(): LiveData<List<BugEvent>> =
        Transformations.map(bugDao.getAllBugEvents()) { bugEvents ->
            bugEvents.map { bugEvent -> bugEvent }
        }

    override fun get(id: Long): LiveData<BugEvent> =
        Transformations.map(bugDao.findBugEvent(id)) { it }

    override suspend fun save(bugEvent: BugEvent) {
        val bugEventEntity: BugEventEntity = mapToBugEventEntity(bugEvent)
        bugDao.insertBugEvent(bugEventEntity)
    }

    private fun mapToBugEventEntity(bugEvent: BugEvent): BugEventEntity =
        when (bugEvent is BugEventEntity) {
            true -> bugEvent
            else -> BugEventEntity(
                bugEvent.createdAt,
                bugEvent.tag,
                bugEvent.info,
                bugEvent.exceptionClass,
                bugEvent.exceptionMessage,
                bugEvent.stackTrace,
                bugEvent.appVersionName,
                bugEvent.appVersionCode,
                bugEvent.apiLevel,
                bugEvent.androidVersion,
                bugEvent.shortID,
                bugEvent.logHistory
            )
        }

    override suspend fun clear() {
        bugDao.deleteAllBugEvents()
    }
}
