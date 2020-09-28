package de.rki.coronawarnapp.environment.download

import android.content.Context
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.environment.EnvironmentSetup
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest

class DownloadCDNModuleTest : BaseIOTest() {

    private fun createModule() = DownloadCDNModule()
    private val validUrl = "http://coronawarn-test.com/Download"
    private val inValidUrl = "Biryani"

    @MockK
    private lateinit var environmentSetup: EnvironmentSetup

    @BeforeEach
    fun setUp(){
        MockKAnnotations.init(this)
        every { environmentSetup.cdnUrlDownload } returns validUrl
    }

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
        environmentSetup.cdnUrlDownload shouldBe validUrl
    }

    @Test
    fun `invalid downloaded URL comes from environment`() {
        every { environmentSetup.cdnUrlDownload } returns inValidUrl
        shouldThrowAny {
            environmentSetup.cdnUrlDownload shouldBe validUrl
        }
    }
}
