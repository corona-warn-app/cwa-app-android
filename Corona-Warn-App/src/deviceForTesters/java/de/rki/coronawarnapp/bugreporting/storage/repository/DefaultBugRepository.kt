package de.rki.coronawarnapp.bugreporting.storage.repository

import de.rki.coronawarnapp.bugreporting.event.BugEvent
import de.rki.coronawarnapp.bugreporting.event.BugEventEntity
import de.rki.coronawarnapp.bugreporting.storage.dao.DefaultBugDao
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBugRepository @Inject constructor(
    private val bugDao: DefaultBugDao
) : BugRepository {

    override fun getAll(): Flow<List<BugEvent>> = bugDao.getAllBugEvents()

    override fun get(id: Long): Flow<BugEvent> = bugDao.getBugEvent(id)

    override suspend fun save(bugEvent: BugEvent) {
        val bugEventEntity: BugEventEntity = bugEvent.mapToBugEventEntity()
        bugDao.insertBugEvent(bugEventEntity)
    }

    private fun BugEvent.mapToBugEventEntity(): BugEventEntity =
        when (this is BugEventEntity) {
            true -> this
            else -> BugEventEntity(
                createdAt = createdAt,
                tag = tag,
                info = info,
                exceptionClass = exceptionClass,
                exceptionMessage = exceptionMessage,
                stackTrace = stackTrace,
                deviceInfo = deviceInfo,
                appVersionName = appVersionName,
                appVersionCode = appVersionCode,
                apiLevel = apiLevel,
                androidVersion = androidVersion,
                shortCommitHash = shortCommitHash,
                logHistory = logHistory
            )
        }

    override suspend fun clear() {
        Timber.d("Deleting all bug events!")
        bugDao.deleteAllBugEvents()
    }
}
