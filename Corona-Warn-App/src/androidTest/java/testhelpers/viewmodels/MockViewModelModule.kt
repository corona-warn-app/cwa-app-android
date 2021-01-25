package testhelpers.viewmodels

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory

@Module
class MockViewModelModule {

    @Provides
    fun viewModelCreators(): MutableMap<Class<out CWAViewModel>, CWAViewModelFactory<out CWAViewModel>> =
        CREATORS

    companion object {
        val CREATORS: MutableMap<Class<out CWAViewModel>, CWAViewModelFactory<out CWAViewModel>> =
            mutableMapOf()
    }
}
