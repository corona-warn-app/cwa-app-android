package de.rki.coronawarnapp.bugreporting.storage.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import de.rki.coronawarnapp.bugreporting.event.BugEvent
import de.rki.coronawarnapp.bugreporting.event.BugEventEntity
import de.rki.coronawarnapp.bugreporting.storage.dao.DefaultBugDao
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
                createdAt = bugEvent.createdAt,
                tag = bugEvent.tag,
                info = bugEvent.info,
                exceptionClass = bugEvent.exceptionClass,
                exceptionMessage = bugEvent.exceptionMessage,
                stackTrace = bugEvent.stackTrace,
                deviceInfo = bugEvent.deviceInfo,
                appVersionName = bugEvent.appVersionName,
                appVersionCode = bugEvent.appVersionCode,
                apiLevel = bugEvent.apiLevel,
                androidVersion = bugEvent.androidVersion,
                shortCommitHash = bugEvent.shortCommitHash,
                logHistory = bugEvent.logHistory
            )
        }

    override suspend fun clear() {
        bugDao.deleteAllBugEvents()
    }
}
