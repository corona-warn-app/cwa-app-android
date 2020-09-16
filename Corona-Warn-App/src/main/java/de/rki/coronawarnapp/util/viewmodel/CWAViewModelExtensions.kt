package de.rki.coronawarnapp.util.viewmodel

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle

@MainThread
inline fun <reified VM : CWAViewModel> Fragment.cwaViewModels(
    noinline factoryProducer: (() -> CWAViewModelSource.Factory)
) = this.cwaViewModels<VM>(null, factoryProducer)

@MainThread
inline fun <reified VM : CWAViewModel> Fragment.cwaViewModels(
    noinline keyProducer: (() -> String)? = null,
    noinline factoryProducer: (() -> CWAViewModelSource.Factory)
) = viewModelsKeyed<VM>(keyProducer) { factoryProducer.invoke().create(this, arguments) }

@MainThread
inline fun <reified VM : CWAViewModel> Fragment.cwaViewModelsAssisted(
    noinline factoryProducer: (() -> CWAViewModelSource.Factory),
    noinline constructorCall: ((CWAViewModelFactory<out CWAViewModel>, SavedStateHandle) -> CWAViewModel)
) = this.cwaViewModelsAssisted<VM>(null, factoryProducer, constructorCall)

@MainThread
inline fun <reified VM : CWAViewModel> Fragment.cwaViewModelsAssisted(
    noinline keyProducer: (() -> String)? = null,
    noinline factoryProducer: (() -> CWAViewModelSource.Factory),
    noinline constructorCall: ((CWAViewModelFactory<out CWAViewModel>, SavedStateHandle) -> CWAViewModel)
) = viewModelsKeyed<VM>(keyProducer) {
    factoryProducer.invoke().create(this, arguments, constructorCall)
}

@MainThread
inline fun <reified VM : CWAViewModel> ComponentActivity.cwaViewModels(
    noinline factoryProducer: (() -> CWAViewModelSource.Factory)
) = this.cwaViewModels<VM>(null, factoryProducer)

@MainThread
inline fun <reified VM : CWAViewModel> ComponentActivity.cwaViewModels(
    noinline keyProducer: (() -> String)? = null,
    noinline factoryProducer: (() -> CWAViewModelSource.Factory)
) = viewModelsKeyed<VM>(keyProducer) { factoryProducer.invoke().create(this, intent.extras) }

@MainThread
inline fun <reified VM : CWAViewModel> ComponentActivity.cwaViewModelsAssisted(
    noinline factoryProducer: (() -> CWAViewModelSource.Factory),
    noinline constructorCall: ((CWAViewModelFactory<out CWAViewModel>, SavedStateHandle) -> CWAViewModel)
) = this.cwaViewModelsAssisted<VM>(null, factoryProducer, constructorCall)

@MainThread
inline fun <reified VM : CWAViewModel> ComponentActivity.cwaViewModelsAssisted(
    noinline keyProducer: (() -> String)? = null,
    noinline factoryProducer: (() -> CWAViewModelSource.Factory),
    noinline constructorCall: ((CWAViewModelFactory<out CWAViewModel>, SavedStateHandle) -> CWAViewModel)
) = viewModelsKeyed<VM>(keyProducer) {
    factoryProducer.invoke().create(this, intent.extras, constructorCall)
}
