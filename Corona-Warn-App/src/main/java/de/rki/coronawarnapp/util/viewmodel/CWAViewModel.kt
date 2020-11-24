package de.rki.coronawarnapp.util.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rki.coronawarnapp.util.coroutine.DefaultDispatcherProvider
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

abstract class CWAViewModel constructor(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
    private val childViewModels: List<CWAViewModel> = emptyList()
) : ViewModel() {

    private val tag: String = this::class.simpleName!!

    init {
        Timber.tag(tag).v("Initialized")
    }

    /**
     * This launches a coroutine on another thread
     * Remember to switch to the main thread if you want to update the UI directly
     */
    fun launch(
        context: CoroutineContext = dispatcherProvider.Default,
        block: suspend CoroutineScope.() -> Unit
    ) {
        try {
            viewModelScope.launch(context = context, block = block)
        } catch (e: CancellationException) {
            Timber.w(e, "launch()ed coroutine was canceled.")
        }
    }

    @CallSuper
    override fun onCleared() {
        Timber.tag(tag).v("onCleared()")
        childViewModels.forEach {
            Timber.tag(tag).v("Clearing child VM: %s", it)
            it.onCleared()
        }
        super.onCleared()
    }
}
