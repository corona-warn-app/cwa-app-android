package de.rki.coronawarnapp.bugreporting

import de.rki.coronawarnapp.util.CWADebug
import timber.log.Timber

interface BugReporter {
    fun report(throwable: Throwable, tag: String? = null, info: String? = null)
}

fun Throwable.reportProblem(tag: String? = null, info: String? = null) {
    Timber.tag(tag ?: "BugReporter").e(this, info)

    if (CWADebug.isAUnitTest) return
    val reporter = AppInjector.component.bugReporter
    reporter.report(this, tag, info)
}
