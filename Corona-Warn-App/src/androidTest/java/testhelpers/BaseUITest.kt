package testhelpers

import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import testhelpers.viewmodels.MockViewModelModule

abstract class BaseUITest : BaseTest() {

    inline fun <reified T : CWAViewModel> setupMockViewModel(factory: SimpleCWAViewModelFactory<T>) {
        MockViewModelModule.CREATORS[T::class.java] = factory
    }

    fun clearAllViewModels() {
        MockViewModelModule.CREATORS.clear()
    }
}
