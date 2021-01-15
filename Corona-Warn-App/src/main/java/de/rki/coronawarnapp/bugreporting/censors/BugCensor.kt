package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine

interface BugCensor {

    /**
     * If there is something to censor a new log line is returned, otherwise returns null
     */
    suspend fun checkLog(entry: LogLine): LogLine?
}
