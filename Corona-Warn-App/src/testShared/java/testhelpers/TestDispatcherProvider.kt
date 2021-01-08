package testhelpers

import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object TestDispatcherProvider : DispatcherProvider {
    override val Default: CoroutineDispatcher
        get() = Dispatchers.Unconfined
    override val Main: CoroutineDispatcher
        get() = Dispatchers.Unconfined
    override val MainImmediate: CoroutineDispatcher
        get() = Dispatchers.Unconfined
    override val Unconfined: CoroutineDispatcher
        get() = Dispatchers.Unconfined
    override val IO: CoroutineDispatcher
        get() = Dispatchers.Unconfined
}
