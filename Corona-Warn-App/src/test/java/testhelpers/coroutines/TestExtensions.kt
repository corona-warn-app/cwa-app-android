package testhelpers.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * If you have a test that uses a coroutine that never stops, you may use this.
 */

@ExperimentalCoroutinesApi // Since 1.2.1, tentatively till 1.3.0
fun TestScope.runTest2(
    ignoreActive: Boolean = false,
    block: suspend TestScope.() -> Unit
): Unit = runTest2(
    ignoreActive = ignoreActive,
    context = coroutineContext,
    testBody = block
)

fun runTest2(
    ignoreActive: Boolean = false,
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend TestScope.() -> Unit
) {
    try {
        runBlocking {
            val job = launch {
                runTest(
                    context = context,
                    dispatchTimeoutMs = 1_00L,
                    testBody = testBody
                )
            }

            job.cancelAndJoin()
        }
    } catch (e: Exception) {
        if (!ignoreActive || (e.message != "This job has not completed yet")) {
            throw e
        }
    }
}
