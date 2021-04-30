package de.rki.coronawarnapp.bugreporting.censors.submission

import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine

class PcrCoronaTestCensor : BugCensor {

    override suspend fun checkLog(entry: LogLine): LogLine? {
        TODO("Not yet implemented")
    }
}
