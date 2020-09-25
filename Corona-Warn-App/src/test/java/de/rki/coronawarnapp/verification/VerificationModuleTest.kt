package de.rki.coronawarnapp.verification

import de.rki.coronawarnapp.BuildConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest

class VerificationModuleTest : BaseIOTest() {

    private val module = VerificationModule()

    @Test
    fun `verification URL comes from BuildConfig`() {
        module.provideVerificationUrl() shouldBe BuildConfig.VERIFICATION_CDN_URL
    }
}
