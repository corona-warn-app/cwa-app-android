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
    initAction: suspend () -> T
) = SmartLiveDataProperty(dispatcher, initAction)

class SmartLiveDataProperty<T : Any>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val initialValueProvider: suspend () -> T,
) : ReadOnlyProperty<ViewModel, SmartLiveData<T>> {

    private var liveData: SmartLiveData<T>? = null

    override fun getValue(
        thisRef: ViewModel,
        property: KProperty<*>
    ): SmartLiveData<T> {
        liveData?.let {
            return@getValue it
        }

        return SmartLiveData<T>(thisRef, dispatcher).also {
            liveData = it
            thisRef.viewModelScope.launch(context = dispatcher) {
                it.postValue(initialValueProvider())
            }
        }
    }
}

class SmartLiveData<T : Any>(
    private val viewModel: ViewModel,
    private val dispatcher: CoroutineDispatcher,
) : MutableLiveData<T>() {

    fun update(updateAction: (T) -> T) {
        observeOnce {
            viewModel.viewModelScope.launch(context = dispatcher) {
                postValue(updateAction(it))
            }
        }
    }
}
