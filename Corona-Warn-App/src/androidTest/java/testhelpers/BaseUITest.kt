package testhelpers

import android.Manifest
import androidx.test.rule.GrantPermissionRule
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
