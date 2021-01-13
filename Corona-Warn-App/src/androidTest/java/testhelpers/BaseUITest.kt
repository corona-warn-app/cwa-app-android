package testhelpers

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import testhelpers.viewmodels.MockViewModelModule
import tools.fastlane.screengrab.Screengrab

abstract class BaseUITest : BaseTest() {

    inline fun <reified T : CWAViewModel> setupMockViewModel(factory: CWAViewModelFactory<T>) {
        MockViewModelModule.CREATORS[T::class.java] = factory
    }

    fun clearAllViewModels() {
        MockViewModelModule.CREATORS.clear()
    }

    inline fun <reified F: Fragment>captureScreenshot(suffix:String = "") {
        val name = F::class.simpleName
        launchFragmentInContainer2<F>()
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        Screengrab.screenshot(name.plus(suffix))
    }
}
