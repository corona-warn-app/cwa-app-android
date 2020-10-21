package de.rki.coronawarnapp.bugreporting

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.loghistory.LogHistoryTree
import timber.log.Timber
import javax.inject.Singleton

@Module
class BugReportingModule {

    @Singleton
    @Provides
    fun reporter(): BugReporter = object : BugReporter {
        override fun report(throwable: Throwable, info: String?) {
            // NOOP
        }
    }

    @Singleton
    @LogHistoryTree
    @Provides
    fun loggingHistory(): Timber.Tree = object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // NOOP
        }
    }
}
