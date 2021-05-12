package testhelpers

import io.kotest.assertions.retry
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

/**
 * TODO Remove flaky tests where possible
 * A flaky test is a test that sometimes fails and sometimes doesn't
 * Feel free to find usages of this method and to refactor them such that they are working reliably.
 */
@ExperimentalTime
fun <T> flakyTest(flakyAction: () -> T): Unit = runBlocking {
    retry(
        maxRetry = 5,
        timeout = Duration.seconds(30),
        delay = Duration.seconds(1),
        multiplier = 1,
        exceptionClass = Exception::class,
        f = {
            Timber.v("Flaky test try...")
            flakyAction()
        }
    )
}
