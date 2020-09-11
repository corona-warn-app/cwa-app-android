package de.rki.coronawarnapp.util.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class VDCSource @AssistedInject constructor(
    private val creators: @JvmSuppressWildcards Map<Class<out VDC>, VDCFactory<out VDC>>,
    @Assisted savedStateOwner: SavedStateRegistryOwner,
    @Assisted defaultSavedState: Bundle?,
    @Assisted private val assistAction: ((VDCFactory<out VDC>, SavedStateHandle) -> VDC)?
) : AbstractSavedStateViewModelFactory(savedStateOwner, defaultSavedState) {


    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        val factory = creators.entries.find { modelClass.isAssignableFrom(it.key) }?.value
        if (factory == null) throw IllegalStateException("Unknown VDC factory: $modelClass")

        @Suppress("UNCHECKED_CAST")
        var vm: T? = assistAction?.invoke(factory, handle) as? T

        @Suppress("UNCHECKED_CAST")
        if (vm == null) {
            vm = when (factory) {
                is SavedStateVDCFactory<*> -> factory.create(handle) as? T
                is SimpleVDCFactory<*> -> factory.create() as? T
                else -> throw IllegalStateException("Unknown factory: $factory")
            }
        }

        return vm ?: throw IllegalStateException("VDCFactory $factory didn't return a VDC")
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(
            savedStateOwner: SavedStateRegistryOwner,
            defaultSavedState: Bundle?,
            assistAction: ((VDCFactory<out VDC>, SavedStateHandle) -> VDC)? = null
        ): VDCSource
    }
}
