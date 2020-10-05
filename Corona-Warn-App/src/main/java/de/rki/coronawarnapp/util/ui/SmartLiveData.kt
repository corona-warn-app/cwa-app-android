package de.rki.coronawarnapp.util.ui

import androidx.annotation.MainThread
import androidx.annotation.Nullable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : Any> ViewModel.smartLiveData(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    liveDataFactory: (ViewModel, CoroutineDispatcher) -> SmartLiveData<T> = { vm, disp ->
        SmartLiveData(vm, disp)
    },
    initAction: suspend () -> T
) = SmartLiveDataProperty(dispatcher, initAction, liveDataFactory)

class SmartLiveDataProperty<T : Any, LV : SmartLiveData<T>>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val initialValueProvider: suspend () -> T,
    private val liveDataFactory: (ViewModel, CoroutineDispatcher) -> LV
) : ReadOnlyProperty<ViewModel, SmartLiveData<T>> {

    private var liveData: SmartLiveData<T>? = null

    override fun getValue(
        thisRef: ViewModel,
        property: KProperty<*>
    ): SmartLiveData<T> {
        liveData?.let {
            return@getValue it
        }

        return liveDataFactory(thisRef, dispatcher).also {
            liveData = it
            thisRef.viewModelScope.launch(context = dispatcher) {
                it.postValue(initialValueProvider())
            }
        }
    }
}

class SmartSingleLiveEvent<T : Any>(
    viewModel: ViewModel,
    dispatcher: CoroutineDispatcher
) : SmartLiveData<T>(viewModel, dispatcher) {
    private val pending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Timber.w("Multiple observers registered but only one will be notified of changes.")
        }

        // Observe the internal MutableLiveData
        super.observe(owner, { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(@Nullable t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }
}

open class SmartLiveData<T : Any>(
    private val viewModel: ViewModel,
    private val dispatcher: CoroutineDispatcher
) : MutableLiveData<T>() {

    fun update(updateAction: (T) -> T) {
        observeOnce {
            viewModel.viewModelScope.launch(context = dispatcher) {
                postValue(updateAction(it))
            }
        }
    }
}
