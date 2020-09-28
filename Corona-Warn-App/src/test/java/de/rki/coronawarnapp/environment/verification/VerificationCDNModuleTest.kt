package de.rki.coronawarnapp.environment.verification

import android.content.Context
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.environment.submission.SubmissionCDNModule
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest

class VerificationCDNModuleTest : BaseIOTest() {
    private fun createModule() = VerificationCDNModule()
    private val validUrl = "https://coronawarn-test.com/Verification"
    private val inValidUrl = "Chicken Wings"

    @MockK
    private lateinit var environmentSetup: EnvironmentSetup

    @BeforeEach
    fun setUp(){
        MockKAnnotations.init(this)
    }

    @Test
    fun `sideeffect free instantiation`() {
        shouldNotThrowAny {
            createModule()
        }
    }

    @Test
    fun `valid downloaded URL comes from environment`() {
        every { environmentSetup.cdnUrlVerification } returns validUrl
        environmentSetup.cdnUrlVerification shouldBe validUrl
    }

    @Test
    fun `invalid downloaded URL comes from environment`() {
        every { environmentSetup.cdnUrlVerification } returns inValidUrl
        shouldThrowAny {
            environmentSetup.cdnUrlVerification
        }
    }
}
