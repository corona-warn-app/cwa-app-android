package de.rki.coronawarnapp.rootdetection

import io.kotest.assertions.throwables.shouldNotThrowAny
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RootDetectionModuleTest: BaseTest() {

    @Test
    fun `instantiation without error`() {
        shouldNotThrowAny {
            RootDetectionModule()
        }
    }
}
