package de.rki.coronawarnapp.bugreporting.processor

import de.rki.coronawarnapp.bugreporting.event.BugEvent

interface BugProcessor {
    suspend fun processor(throwable: Throwable, info: String?): BugEvent
}
