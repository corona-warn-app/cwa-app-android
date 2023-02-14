package testhelpers

import android.Manifest
import androidx.test.rule.GrantPermissionRule
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.junit.Rule

abstract class BaseUITest : BaseTestInstrumentation() {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()
}
