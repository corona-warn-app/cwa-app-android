package de.rki.coronawarnapp.util.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
