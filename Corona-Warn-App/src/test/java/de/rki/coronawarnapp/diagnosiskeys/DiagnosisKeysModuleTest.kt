package de.rki.coronawarnapp.diagnosiskeys

import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest

class DiagnosisKeysModuleTest : BaseIOTest() {

    private val module = DiagnosisKeysModule()

    @Test
    fun `home country should be DE`() {
        module.provideDiagnosisHomeCountry() shouldBe LocationCode("DE")
    }

    @Test
    fun `download URL comes from BuildConfig`() {
        module.provideDownloadServerUrl() shouldBe BuildConfig.DOWNLOAD_CDN_URL
    }

}
