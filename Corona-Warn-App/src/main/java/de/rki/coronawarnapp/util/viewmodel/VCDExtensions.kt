package de.rki.coronawarnapp.util.viewmodel

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle

@MainThread
inline fun <reified VM : VDC> Fragment.vdcs(
    noinline factoryProducer: (() -> VDCSource.Factory)
) = this.vdcs<VM>(null, factoryProducer)

@MainThread
inline fun <reified VM : VDC> Fragment.vdcs(
    noinline keyProducer: (() -> String)? = null,
    noinline factoryProducer: (() -> VDCSource.Factory)
) = viewModelsKeyed<VM>(keyProducer) { factoryProducer.invoke().create(this, arguments) }

@MainThread
inline fun <reified VM : VDC> Fragment.vdcsAssisted(
    noinline factoryProducer: (() -> VDCSource.Factory),
    noinline constructorCall: ((VDCFactory<out VDC>, SavedStateHandle) -> VDC)
) = this.vdcsAssisted<VM>(null, factoryProducer, constructorCall)

@MainThread
inline fun <reified VM : VDC> Fragment.vdcsAssisted(
    noinline keyProducer: (() -> String)? = null,
    noinline factoryProducer: (() -> VDCSource.Factory),
    noinline constructorCall: ((VDCFactory<out VDC>, SavedStateHandle) -> VDC)
) = viewModelsKeyed<VM>(keyProducer) {
    factoryProducer.invoke().create(this, arguments, constructorCall)
}

@MainThread
inline fun <reified VM : VDC> ComponentActivity.vdcs(
    noinline factoryProducer: (() -> VDCSource.Factory)
) = this.vdcs<VM>(null, factoryProducer)

@MainThread
inline fun <reified VM : VDC> ComponentActivity.vdcs(
    noinline keyProducer: (() -> String)? = null,
    noinline factoryProducer: (() -> VDCSource.Factory)
) = viewModelsKeyed<VM>(keyProducer) { factoryProducer.invoke().create(this, intent.extras) }

@MainThread
inline fun <reified VM : VDC> ComponentActivity.vdcsAssisted(
    noinline factoryProducer: (() -> VDCSource.Factory),
    noinline constructorCall: ((VDCFactory<out VDC>, SavedStateHandle) -> VDC)
) = this.vdcsAssisted<VM>(null, factoryProducer, constructorCall)

@MainThread
inline fun <reified VM : VDC> ComponentActivity.vdcsAssisted(
    noinline keyProducer: (() -> String)? = null,
    noinline factoryProducer: (() -> VDCSource.Factory),
    noinline constructorCall: ((VDCFactory<out VDC>, SavedStateHandle) -> VDC)
) = viewModelsKeyed<VM>(keyProducer) {
    factoryProducer.invoke().create(this, intent.extras, constructorCall)
}
