package de.rki.coronawarnapp.environment.download

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
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

class DownloadCDNModuleTest : BaseIOTest() {

    private val validUrl = "https://coronawarn-test.com/Download"
    private val inValidUrl = "Biryani"

    @MockK
    private lateinit var environmentSetup: EnvironmentSetup

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { environmentSetup.downloadCdnUrl } returns validUrl
    }

    private fun createModule() = DownloadCDNModule()

    @Test
    fun `sideeffect free instantiation`() {
        shouldNotThrowAny {
            createModule()
        }
    }

    @Test
    fun `home country should be DE`() {
        val module = createModule()
        module.provideDiagnosisHomeCountry() shouldBe LocationCode("DE")
    }

    @Test
    fun `valid downloaded URL comes from environment`() {
        val module = createModule()
        module.provideDownloadServerUrl(environmentSetup) shouldBe validUrl
    }

    @Test
    fun `invalid downloaded URL comes from environment`() {
        every { environmentSetup.downloadCdnUrl } returns inValidUrl
        val module = createModule()
        shouldThrowAny {
            module.provideDownloadServerUrl(environmentSetup) shouldBe validUrl
        }
    }
}
