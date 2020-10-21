package de.rki.coronawarnapp.bugreporting

import de.rki.coronawarnapp.util.di.AppInjector

interface BugReporter {
    fun report(throwable: Throwable, info: String? = null)
}

fun Throwable.reportProblem(info: String? = null) {
    val reporter = AppInjector.component.bugReporter
    reporter.report(this, info)
}
