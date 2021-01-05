package de.rki.coronawarnapp.bugreporting.debuglog

import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import javax.inject.Inject

/**
 * Workaround for dagger injection into kotlin objects
 */
@Suppress("UnnecessaryAbstractClass")
abstract class DebugLoggerBase {
    @Inject internal lateinit var bugCensors: dagger.Lazy<List<BugCensor>>
}
