package de.rki.coronawarnapp.util.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rki.coronawarnapp.util.coroutine.DefaultDispatcherProvider
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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

    fun launch(
        context: CoroutineContext = dispatcherProvider.Main,
        block: suspend CoroutineScope.() -> Unit
    ): Job = viewModelScope.launch(context = context, block = block)

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
