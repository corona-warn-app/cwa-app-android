package testhelpers.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    ignoreActive: Boolean = true,
    context: CoroutineContext = UnconfinedTestDispatcher(),
    testBody: suspend TestScope.() -> Unit
) {
    runTest(
        context = context
    ) {
        testBody()
        if (ignoreActive) coroutineContext.cancelChildren()
    }
}
