package de.rki.coronawarnapp.bugreporting.event

import org.joda.time.Instant
import java.util.UUID
import kotlin.reflect.KClass

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
