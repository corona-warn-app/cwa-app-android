package testhelpers

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.junit.Rule
import testhelpers.viewmodels.MockViewModelModule

abstract class BaseUITest : BaseTestInstrumentation() {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    inline fun <reified T : CWAViewModel> setupMockViewModel(factory: CWAViewModelFactory<T>) {
        MockViewModelModule.CREATORS[T::class.java] = factory
    }

    fun clearAllViewModels() {
        MockViewModelModule.CREATORS.clear()
    }
}
