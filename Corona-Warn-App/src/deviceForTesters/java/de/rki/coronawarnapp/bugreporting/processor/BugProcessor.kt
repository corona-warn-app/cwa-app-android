package de.rki.coronawarnapp.bugreporting.processor

import de.rki.coronawarnapp.bugreporting.event.BugEvent

interface BugProcessor {
    suspend fun processor(throwable: Throwable, tag: String?, info: String?): BugEvent
}
