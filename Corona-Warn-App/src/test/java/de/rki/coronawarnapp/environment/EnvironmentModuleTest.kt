package de.rki.coronawarnapp.environment

import io.kotest.assertions.throwables.shouldNotThrowAny
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class EnvironmentModuleTest : BaseTest() {

    private fun createModule() = EnvironmentModule()

    @Test
    fun `sideeffect free instantiation`() {
        shouldNotThrowAny {
            createModule()
        }
    }
}
