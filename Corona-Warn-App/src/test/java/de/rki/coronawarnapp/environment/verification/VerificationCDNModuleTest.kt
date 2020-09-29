package de.rki.coronawarnapp.environment.verification

import de.rki.coronawarnapp.environment.EnvironmentSetup
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest

class VerificationCDNModuleTest : BaseIOTest() {
    private val validUrl = "https://coronawarn-test.com/Verification"
    private val inValidUrl = "Chicken Wings"

    @MockK lateinit var environmentSetup: EnvironmentSetup

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createModule() = VerificationCDNModule()

    @Test
    fun `sideeffect free instantiation`() {
        shouldNotThrowAny {
            createModule()
        }
    }

    @Test
    fun `valid downloaded URL comes from environment`() {
        every { environmentSetup.verificationCdnUrl } returns validUrl
        val module = VerificationCDNModule()
        module.provideVerificationUrl(environmentSetup) shouldBe validUrl
    }

    @Test
    fun `invalid downloaded URL comes from environment`() {
        every { environmentSetup.verificationCdnUrl } returns inValidUrl
        shouldThrowAny {
            val module = VerificationCDNModule()
            module.provideVerificationUrl(environmentSetup)
        }
    }
}
