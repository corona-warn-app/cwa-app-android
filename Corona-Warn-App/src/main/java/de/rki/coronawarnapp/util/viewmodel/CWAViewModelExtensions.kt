package de.rki.coronawarnapp.util.viewmodel

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore

@MainThread
inline fun <reified VM : CWAViewModel> Fragment.cwaViewModels(
    noinline keyProducer: (() -> String)? = null,
    noinline ownerProducer: () -> ViewModelStore = { this.viewModelStore },
    noinline factoryProducer: (() -> CWAViewModelFactoryProvider.Factory)
) = createViewModelLazyKeyed(
    VM::class,
    keyProducer,
    ownerProducer
) { factoryProducer.invoke().create(this, arguments) }

@MainThread
inline fun <reified VM : CWAViewModel> Fragment.cwaViewModelsAssisted(
    noinline keyProducer: (() -> String)? = null,
    noinline ownerProducer: () -> ViewModelStore = { this.viewModelStore },
    noinline factoryProducer: (() -> CWAViewModelFactoryProvider.Factory),
    noinline constructorCall: ((CWAViewModelFactory<out CWAViewModel>, SavedStateHandle) -> CWAViewModel)
) = createViewModelLazyKeyed(
    VM::class,
    keyProducer,
    ownerProducer
) { factoryProducer.invoke().create(this, arguments, constructorCall) }

@MainThread
inline fun <reified VM : CWAViewModel> ComponentActivity.cwaViewModels(
    noinline keyProducer: (() -> String)? = null,
    noinline ownerProducer: () -> ViewModelStore = { this.viewModelStore },
    noinline factoryProducer: (() -> CWAViewModelFactoryProvider.Factory)
) = createViewModelLazyKeyed(
    VM::class,
    keyProducer,
    ownerProducer
) { factoryProducer.invoke().create(this, intent.extras) }

@MainThread
inline fun <reified VM : CWAViewModel> ComponentActivity.cwaViewModelsAssisted(
    noinline keyProducer: (() -> String)? = null,
    noinline ownerProducer: () -> ViewModelStore = { this.viewModelStore },
    noinline factoryProducer: (() -> CWAViewModelFactoryProvider.Factory),
    noinline constructorCall: ((CWAViewModelFactory<out CWAViewModel>, SavedStateHandle) -> CWAViewModel)
) = createViewModelLazyKeyed(
    VM::class,
    keyProducer,
    ownerProducer
) { factoryProducer.invoke().create(this, intent.extras, constructorCall) }

@MainThread
inline fun <reified T : ViewModel> Fragment.assistedViewModel(
    crossinline viewModelProducer: (SavedStateHandle) -> T
) = viewModels<T> {
    object : AbstractSavedStateViewModelFactory(this, arguments) {
        override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle) =
            viewModelProducer(handle) as T
    }
}
