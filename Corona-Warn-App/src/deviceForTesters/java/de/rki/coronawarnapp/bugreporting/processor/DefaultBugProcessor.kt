package de.rki.coronawarnapp.bugreporting.processor

import android.content.Context
import de.rki.coronawarnapp.bugreporting.event.BugEvent
import de.rki.coronawarnapp.bugreporting.loghistory.RollingLogHistory
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBugProcessor @Inject constructor(
    @AppContext private val context: Context,
    private val timeStamper: TimeStamper,
    private val rollingLogHistory: RollingLogHistory
) : BugProcessor {

    override suspend fun processor(throwable: Throwable, info: String?): BugEvent {
//        val processedEvent = ProcessedEvent(
//            ...
//        )
        TODO("exceptionMessage = Exception().tryFormattedError(context)")
        TODO("rollingLogHistory.getLast(...)")
        TODO("Not yet implemented")
    }
}
