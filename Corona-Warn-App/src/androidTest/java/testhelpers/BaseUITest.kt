package testhelpers

import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import testhelpers.viewmodels.MockViewModelModule

abstract class BaseUITest : BaseTest() {

    inline fun <reified T : CWAViewModel> setupMockViewModel(factory: CWAViewModelFactory<T>) {
        MockViewModelModule.CREATORS[T::class.java] = factory
    }

    fun clearAllViewModels() {
        MockViewModelModule.CREATORS.clear()
    }
}
