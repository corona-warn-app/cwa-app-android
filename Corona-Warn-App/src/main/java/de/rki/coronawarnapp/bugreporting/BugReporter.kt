package de.rki.coronawarnapp.bugreporting

import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.AppInjector
import timber.log.Timber

interface BugReporter {
    fun report(throwable: Throwable, tag: String? = null, info: String? = null)
}

fun Throwable.reportProblem(tag: String? = null, info: String? = null) {
    Timber.tag("BugReporter").v(this, "report(tag=$tag, info=$info)")

    if (CWADebug.isAUnitTest) return
    val reporter = AppInjector.component.bugReporter
    reporter.report(this, tag, info)
}
