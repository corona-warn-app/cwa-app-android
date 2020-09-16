package de.rki.coronawarnapp.util.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

/**
 * Using fragments as example:
 *
 * Step 1: Inject CWAViewModelSource.Factory into Fragment
 *
 * Step 2: Use assisted inject `CWAViewModelSource.Factory.create(...)` to create an instance of
 * CWAViewModelSource that has access to the fragments `savedStateOwner`
 *
 * Step 3: On first access (like "by lazy") to the viewModel the extension function,
 * i.e. `ourViewModel.onActionClicked`,
 * `cwaViewModelsAssisted` (or one of it's variants) checks the fragments viewmodel store
 *
 * Step 4: If no viewmodel was available, this factory is tasked with creating a new viewmodel
 *
 * Step 5: `CWAViewModelSource.create` is called as part of that flow and returns a new viewmodel
 *
 * Step 6: The new viewmodel is cached and returned
 *
 * Step 7: Finally whatever called `ourViewModel.onActionClicked` can execute.
 */
class CWAViewModelFactoryProvider @AssistedInject constructor(
    private val creators: @JvmSuppressWildcards Map<Class<out CWAViewModel>, CWAViewModelFactory<out CWAViewModel>>,
    @Assisted savedStateOwner: SavedStateRegistryOwner,
    @Assisted defaultSavedState: Bundle?,
    @Assisted private val assistAction: ((CWAViewModelFactory<out CWAViewModel>, SavedStateHandle) -> CWAViewModel)?
) : AbstractSavedStateViewModelFactory(savedStateOwner, defaultSavedState) {

    /**
     * Called indirectly (lazy) when the viewModel is accessed in a Fragment/Activity
     **/
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        // Find the right factory for our ViewModel
        val factory = creators.entries.find { modelClass.isAssignableFrom(it.key) }?.value
        if (factory == null) throw IllegalStateException("Unknown ViewModel factory: $modelClass")

        // If an `assistAction` was passed from `cwaViewModels` use that to create the ViewModel
        @Suppress("UNCHECKED_CAST")
        var vm: T? = assistAction?.invoke(factory, handle) as? T

        // If no `assistAction` was passed, try one of the defaults
        // The Fragment or Activity may have used one of them to reduce boilerplate code.
        @Suppress("UNCHECKED_CAST")
        if (vm == null) {
            vm = when (factory) {
                is SavedStateCWAViewModelFactory<*> -> factory.create(handle) as? T
                is SimpleCWAViewModelFactory<*> -> factory.create() as? T
                else -> throw IllegalStateException("Unknown factory: $factory")
            }
        }

        return vm ?: throw IllegalStateException("$factory didn't return a ViewModel")
    }

    /**
     * Injected into fragments/activities
     * Uses assisted injection to get access to the fragments/activities SavedStateRegistryOwner
     */
    @AssistedInject.Factory
    interface Factory {
        fun create(
            savedStateOwner: SavedStateRegistryOwner,
            defaultSavedState: Bundle?,
            assistAction: ((CWAViewModelFactory<out CWAViewModel>, SavedStateHandle) -> CWAViewModel)? = null
        ): CWAViewModelFactoryProvider
    }
}
