package de.rki.coronawarnapp.bugreporting.processor

import de.rki.coronawarnapp.bugreporting.BugEvent
import de.rki.coronawarnapp.bugreporting.loghistory.RollingLogHistory
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import org.joda.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class DefaultBugProcessor @Inject constructor(
    @AppContext private val context: String,
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

    data class ProcessedEvent(
        override val id: UUID,
        override val createdAt: Instant,
        override val info: String?,
        override val logHistory: List<String>
    ) : BugEvent {
        override val exceptionClass: KClass<out Throwable>
            get() = TODO("Not yet implemented")
        override val exceptionMessage: String?
            get() = TODO("Not yet implemented")
        override val stackTrace: List<String>
            get() = TODO("Not yet implemented")
    }
}
