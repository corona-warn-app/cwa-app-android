package de.rki.coronawarnapp.coronatest.type.rapidantigen

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RapidAntigenCoronaTestExtensionsTest : BaseTest() {

    @Test
    fun `state determination, unregistered test`() = runBlockingTest {
        val test: RapidAntigenCoronaTest? = null
        test.toSubmissionState() shouldBe SubmissionStateRAT.NoTest
    }
}
