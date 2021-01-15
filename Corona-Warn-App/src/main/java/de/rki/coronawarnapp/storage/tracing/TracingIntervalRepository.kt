package de.rki.coronawarnapp.storage.tracing

import android.content.Context
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.AppDatabase
import timber.log.Timber

class TracingIntervalRepository(private val tracingIntervalDao: TracingIntervalDao) {

    companion object {
        private val TAG: String? = TracingIntervalRepository::class.simpleName

        @Volatile
        private var instance: TracingIntervalRepository? = null

        private fun getInstance(tracingIntervalDao: TracingIntervalDao) =
            instance ?: synchronized(this) {
                instance
                    ?: TracingIntervalRepository(tracingIntervalDao)
                        .also { instance = it }
            }

        fun resetInstance() = synchronized(this) {
            instance = null
        }

        fun getDateRepository(context: Context): TracingIntervalRepository {
            return getInstance(
                AppDatabase.getInstance(context.applicationContext)
                    .tracingIntervalDao()
            )
        }
    }

    suspend fun createInterval(from: Long, to: Long) {
        Timber.v("Insert Tracing Interval $from, $to")
        if (to < from) throw IllegalArgumentException("to cannot be before from")
        tracingIntervalDao.insertInterval(TracingIntervalEntity().apply {
            this.from = from
            this.to = to
        })
    }

    suspend fun getIntervals(): List<Pair<Long, Long>> {
        val retentionTimestamp = System.currentTimeMillis() - TimeVariables.getDefaultRetentionPeriodInMS()
        tracingIntervalDao.deleteOutdatedIntervals(retentionTimestamp)

        return tracingIntervalDao
            .getAllIntervals()
            .map { Pair(it.from, it.to) }
            .also { Timber.d("Intervals: $it") }
    }
}
