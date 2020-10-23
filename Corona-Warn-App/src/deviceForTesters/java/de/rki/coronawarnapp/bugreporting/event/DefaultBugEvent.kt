package de.rki.coronawarnapp.bugreporting.event

import org.joda.time.Instant
import java.util.UUID
import kotlin.reflect.KClass

data class DefaultBugEvent(
    override val id: UUID,
    override val createdAt: Instant,
    override val info: String?,
    override val exceptionClass: KClass<out Throwable>,
    override val exceptionMessage: String?,
    override val stackTrace: List<String>,
    override val logHistory: List<String>
): BugEvent {
}
