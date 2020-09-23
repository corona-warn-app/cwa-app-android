package de.rki.coronawarnapp.environment.download

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest

class DownloadCDNModuleTest : BaseIOTest() {

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
    fun `download URL comes from environment`() {
        TODO()
//        val module = createModule()
//        module.provideDownloadServerUrl() shouldBe BuildConfig.DOWNLOAD_CDN_URL
    }
}
