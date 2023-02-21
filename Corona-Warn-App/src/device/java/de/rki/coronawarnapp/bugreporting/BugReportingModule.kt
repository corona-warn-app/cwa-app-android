package de.rki.coronawarnapp.bugreporting

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.bugreporting.loghistory.LogHistoryTree
import timber.log.Timber
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BugReportingModule {

    @Singleton
    @Provides
    fun reporter(): BugReporter = object : BugReporter {
        override fun report(throwable: Throwable, tag: String?, info: String?) {
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
