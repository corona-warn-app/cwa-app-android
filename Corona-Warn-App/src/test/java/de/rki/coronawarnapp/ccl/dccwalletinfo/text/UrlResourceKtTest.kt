package de.rki.coronawarnapp.ccl.dccwalletinfo.text

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import java.util.Locale

internal class UrlResourceKtTest : BaseTest() {

    @Test
    fun urlResource() {
        val deUrl by urlResource("dcc_admission_state", Locale.GERMAN)
        deUrl shouldBe "https://www.coronawarn.app/de/faq/#dcc_admission_state"

        val enUrl by urlResource("dcc_admission_state", Locale.FRENCH)
        enUrl shouldBe "https://www.coronawarn.app/en/faq/#dcc_admission_state"

        val url by urlResource("dcc_admission_state", Locale.ENGLISH)
        url shouldBe "https://www.coronawarn.app/en/faq/#dcc_admission_state"

        val nullUrl by urlResource(null, Locale.ENGLISH)
        nullUrl shouldBe null
    }
}
