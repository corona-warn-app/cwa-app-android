package testhelpers

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Enters UI demo mode (clean up device status bar)
 * before running screenshot tests and exists it afterwards
 */
class SystemUIDemoModeRule : TestRule {

    private val helper = SystemUIDemoModeHelper()

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                helper.enter()
                base.evaluate()
                helper.exit()
            }
        }
}
