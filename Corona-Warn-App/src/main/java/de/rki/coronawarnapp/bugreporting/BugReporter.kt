package de.rki.coronawarnapp.bugreporting

import de.rki.coronawarnapp.util.di.AppInjector

interface BugReporter {
    fun report(throwable: Throwable, tag: String? = null, info: String? = null)

    companion object {
        val isAUnitTest: Boolean by lazy {
            try {
                Class.forName("testhelpers.IsAUnitTest")
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}

fun Throwable.reportProblem(tag: String? = null, info: String? = null) {
    if (BugReporter.isAUnitTest) return
    val reporter = AppInjector.component.bugReporter
    reporter.report(this, tag, info)
}
