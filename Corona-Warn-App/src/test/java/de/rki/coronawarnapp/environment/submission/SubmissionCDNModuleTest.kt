package de.rki.coronawarnapp.environment.submission

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

class SubmissionCDNModuleTest : BaseIOTest() {

    private val validUrl = "https://coronawarn-test.com/Submission"
    private val inValidUrl = "Tiramisu"

    @MockK
    private lateinit var environmentSetup: EnvironmentSetup

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { environmentSetup.submissionCdnUrl } returns validUrl
    }

    private fun createModule() = SubmissionCDNModule()

    @Test
    fun `sideeffect free instantiation`() {
        shouldNotThrowAny {
            createModule()
        }
    }

    @Test
    fun `valid downloaded URL comes from environment`() {
        val module = createModule()
        module.provideSubmissionUrl(environmentSetup) shouldBe validUrl
    }

    @Test
    fun `invalid downloaded URL comes from environment`() {
        every { environmentSetup.submissionCdnUrl } returns inValidUrl
        val module = createModule()
        shouldThrowAny {
            module.provideSubmissionUrl(environmentSetup)
        }
    }
}
