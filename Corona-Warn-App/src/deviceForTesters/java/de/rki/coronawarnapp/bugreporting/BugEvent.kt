package de.rki.coronawarnapp.bugreporting

import org.joda.time.Instant
import java.util.UUID
import kotlin.reflect.KClass

interface BugEvent {
    val id: UUID
    val createdAt: Instant
    val info: String?
    val exceptionClass: KClass<out Throwable>
    val exceptionMessage: String?
    val stackTrace: List<String>

    // TODO app version
    // TODO checksum
    // Device Fingerprint Build.Fingerprint...
    val logHistory: List<String>
}
