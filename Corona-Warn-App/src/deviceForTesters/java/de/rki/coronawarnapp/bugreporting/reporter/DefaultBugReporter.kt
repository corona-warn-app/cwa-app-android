package de.rki.coronawarnapp.bugreporting.reporter

import de.rki.coronawarnapp.bugreporting.BugReporter
import de.rki.coronawarnapp.bugreporting.processor.BugProcessor
import de.rki.coronawarnapp.bugreporting.storage.repository.BugRepository
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultBugReporter @Inject constructor(
    private val repository: BugRepository,
    private val processor: BugProcessor,
    @AppScope private val scope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider
) : BugReporter {

    override fun report(throwable: Throwable, info: String?) {
        Timber.e(throwable, "Processing reported bug (info=%s).", info)
        scope.launch(context = dispatcherProvider.IO) {
            val event = processor.processor(throwable, info)
            repository.save(event)
        }
    }
}
