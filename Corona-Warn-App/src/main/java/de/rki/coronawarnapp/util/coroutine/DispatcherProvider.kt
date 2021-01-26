package de.rki.coronawarnapp.util.coroutine

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

// Need this to improve testing
// Can currently only replace the main-thread dispatcher.
// https://github.com/Kotlin/kotlinx.coroutines/issues/1365
@Suppress("PropertyName", "VariableNaming")
interface DispatcherProvider {
    val Default: CoroutineContext
        get() = Dispatchers.Default
    val Main: CoroutineContext
        get() = Dispatchers.Main
    val MainImmediate: CoroutineContext
        get() = Dispatchers.Main.immediate
    val Unconfined: CoroutineContext
        get() = Dispatchers.Unconfined
    val IO: CoroutineContext
        get() = Dispatchers.IO
}
