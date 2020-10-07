package testhelpers.viewmodels

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedStateRegistryOwner
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider

@Module
class MockViewModelModule {

    @Provides
    fun viewmodelFactoryProvider(): CWAViewModelFactoryProvider.Factory {
        val factory = object : CWAViewModelFactoryProvider.Factory {
            override fun create(
                savedStateOwner: SavedStateRegistryOwner,
                defaultSavedState: Bundle?,
                assistAction: ((CWAViewModelFactory<out CWAViewModel>, SavedStateHandle) -> CWAViewModel)?
            ): CWAViewModelFactoryProvider {
                return CWAViewModelFactoryProvider(
                    CREATORS,
                    savedStateOwner,
                    defaultSavedState,
                    assistAction
                )
            }
        }
        return factory
    }

    companion object {
        val CREATORS: MutableMap<Class<out CWAViewModel>, CWAViewModelFactory<out CWAViewModel>> =
            mutableMapOf()
    }
}
