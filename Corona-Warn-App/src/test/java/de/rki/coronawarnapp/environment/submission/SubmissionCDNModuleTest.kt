package de.rki.coronawarnapp.environment.submission

import android.content.Context
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.environment.EnvironmentSetup
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import testhelpers.BaseIOTest

class SubmissionCDNModuleTest : BaseIOTest() {

    private fun createModule() = SubmissionCDNModule()
    private val validUrl = "https://coronawarn-test.com/Submission"
    private val inValidUrl = "Tiramisu"

    @MockK
    private lateinit var environmentSetup: EnvironmentSetup

    @BeforeEach
    fun setUp(){
        MockKAnnotations.init(this)
        every { environmentSetup.cdnUrlSubmission } returns validUrl
    }

    @Test
    fun `sideeffect free instantiation`() {
        shouldNotThrowAny {
            createModule()
        }
    }

    @Test
    fun `valid downloaded URL comes from environment`() {
        environmentSetup.cdnUrlSubmission shouldBe validUrl
    }

    @Test
    fun `invalid downloaded URL comes from environment`() {
        every { environmentSetup.cdnUrlSubmission } returns inValidUrl
        shouldThrowAny {
            environmentSetup.cdnUrlSubmission shouldBe validUrl
        }
    }
}
