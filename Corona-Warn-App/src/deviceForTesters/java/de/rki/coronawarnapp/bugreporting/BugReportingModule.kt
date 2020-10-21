package de.rki.coronawarnapp.bugreporting

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.loghistory.LogHistoryTree
import de.rki.coronawarnapp.bugreporting.loghistory.RollingLogHistory
import de.rki.coronawarnapp.bugreporting.processor.BugProcessor
import de.rki.coronawarnapp.bugreporting.processor.DefaultBugProcessor
import de.rki.coronawarnapp.bugreporting.reporter.DefaultBugReporter
import de.rki.coronawarnapp.bugreporting.storage.BugRepository
import de.rki.coronawarnapp.bugreporting.storage.DefaultBugRepository
import timber.log.Timber
import javax.inject.Singleton

@Module
class BugReportingModule {

    @Singleton
    @Provides
    fun reporter(reporter: DefaultBugReporter): BugReporter = reporter

    @Singleton
    @Provides
    fun repository(repository: DefaultBugRepository): BugRepository = repository

    @Singleton
    @Provides
    fun processor(processor: DefaultBugProcessor): BugProcessor = processor

    @Singleton
    @LogHistoryTree
    @Provides
    fun loggingHistory(loggingHistory: RollingLogHistory): Timber.Tree = loggingHistory
}
