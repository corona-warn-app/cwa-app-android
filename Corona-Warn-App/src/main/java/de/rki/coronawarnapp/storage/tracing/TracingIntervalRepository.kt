/******************************************************************************
 * Corona-Warn-App                                                            *
 *                                                                            *
 * SAP SE and all other contributors /                                        *
 * copyright owners license this file to you under the Apache                 *
 * License, Version 2.0 (the "License"); you may not use this                 *
 * file except in compliance with the License.                                *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing,                 *
 * software distributed under the License is distributed on an                *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                     *
 * KIND, either express or implied.  See the License for the                  *
 * specific language governing permissions and limitations                    *
 * under the License.                                                         *
 ******************************************************************************/

package de.rki.coronawarnapp.storage.tracing

import android.content.Context
import android.util.Log
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.AppDatabase

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

        fun getDateRepository(context: Context): TracingIntervalRepository {
            return getInstance(
                AppDatabase.getInstance(context.applicationContext)
                    .tracingIntervalDao()
            )
        }
    }

    suspend fun createInterval(from: Long, to: Long) {
        Log.v(TAG, "Insert Tracing Interval $from, $to")
        if (to < from) throw IllegalArgumentException("to cannot be after from")
        tracingIntervalDao.insertInterval(TracingIntervalEntity().apply {
            this.from = from
            this.to = to
        })
    }

    suspend fun getIntervals(): List<Pair<Long, Long>> {
        deleteOutdatedInterval()
        return tracingIntervalDao.getAllIntervals().map {
            Pair(it.from, it.to)
        }.also {
            Log.d(TAG, "Intervals: $it")
        }
    }

    private suspend fun deleteOutdatedInterval() = tracingIntervalDao
        .deleteOutdatedIntervals(System.currentTimeMillis() - TimeVariables.getDefaultRetentionPeriodInMS())
}
