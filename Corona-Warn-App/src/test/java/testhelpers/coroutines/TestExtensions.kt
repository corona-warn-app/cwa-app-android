package testhelpers.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.UncompletedCoroutinesError
import kotlinx.coroutines.test.runBlockingTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi // Since 1.2.1, tentatively till 1.3.0
fun TestCoroutineScope.runBlockingTest2(
    permanentJobs: Boolean = false,
    block: suspend TestCoroutineScope.() -> Unit
): Unit = runBlockingTest2(
    ignoreActive = permanentJobs,
    context = coroutineContext,
    testBody = block
)

fun runBlockingTest2(
    ignoreActive: Boolean = false,
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend TestCoroutineScope.() -> Unit
) {
    try {
        runBlocking {
            try {
                runBlockingTest(
                    context = context,
                    testBody = testBody
                )
            } catch (e: UncompletedCoroutinesError) {
                if (!ignoreActive) throw e
            }
        }
    } catch (e: Exception) {
        if (!ignoreActive || (e.message != "This job has not completed yet")) {
            throw e
        }
    }
}
