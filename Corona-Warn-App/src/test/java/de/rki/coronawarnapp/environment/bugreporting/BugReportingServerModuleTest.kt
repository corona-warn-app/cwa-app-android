package de.rki.coronawarnapp.environment.bugreporting

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

class BugReportingServerModuleTest : BaseIOTest() {

    private val validUrl = "https://logupload"
    private val inValidUrl = "http://invalid"

    @MockK lateinit var environmentSetup: EnvironmentSetup

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createModule() = BugReportingServerModule()

    @Test
    fun `sideeffect free instantiation`() {
        shouldNotThrowAny {
            createModule()
        }
    }

    @Test
    fun `valid downloaded URL comes from environment`() {
        every { environmentSetup.logUploadServerUrl } returns validUrl
        val module = createModule()
        module.provideBugReportingServerUrl(environmentSetup) shouldBe validUrl
    }

    @Test
    fun `invalid downloaded URL comes from environment`() {
        every { environmentSetup.logUploadServerUrl } returns inValidUrl
        val module = createModule()
        shouldThrowAny {
            module.provideBugReportingServerUrl(environmentSetup) shouldBe validUrl
        }
    }
}
