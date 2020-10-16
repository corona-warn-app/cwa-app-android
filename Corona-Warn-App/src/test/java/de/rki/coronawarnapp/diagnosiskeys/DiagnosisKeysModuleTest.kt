package de.rki.coronawarnapp.diagnosiskeys

import io.kotest.assertions.throwables.shouldNotThrowAny
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DiagnosisKeysModuleTest : BaseTest() {

    private fun createModule() = DiagnosisKeysModule()

    @Test
    fun `sideeffect free instantiation`() {
        shouldNotThrowAny {
            createModule()
        }
    }
}
