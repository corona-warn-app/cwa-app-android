package de.rki.coronawarnapp.coronatest.type.pcr

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PCRCoronaTestExtensionsTest : BaseTest() {

    @Test
    fun `state determination, unregistered test`() = runBlockingTest {
        val test: PCRCoronaTest? = null
        test.toSubmissionState() shouldBe NoTest
    }
}
