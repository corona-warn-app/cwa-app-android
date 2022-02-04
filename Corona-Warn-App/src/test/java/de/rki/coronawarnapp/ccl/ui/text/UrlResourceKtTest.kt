package de.rki.coronawarnapp.ccl.ui.text

import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CCLJsonFunctions
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Locale

internal class UrlResourceKtTest : BaseTest() {
    @MockK private lateinit var cclJsonFunctions: CCLJsonFunctions
    private val mapper = SerializationModule.jacksonBaseMapper

    @Test
    fun formatFaqAnchor() {
        val cclTextFormatter = CCLTextFormatter(cclJsonFunctions, mapper)
        val deUrl = cclTextFormatter.formatFaqAnchor("dcc_admission_state", Locale.GERMAN)
        deUrl shouldBe "https://www.coronawarn.app/de/faq/#dcc_admission_state"

        val enUrl = cclTextFormatter.formatFaqAnchor("dcc_admission_state", Locale.FRENCH)
        enUrl shouldBe "https://www.coronawarn.app/en/faq/#dcc_admission_state"

        val url = cclTextFormatter.formatFaqAnchor("dcc_admission_state", Locale.ENGLISH)
        url shouldBe "https://www.coronawarn.app/en/faq/#dcc_admission_state"

        val nullUrl = cclTextFormatter.formatFaqAnchor(null, Locale.ENGLISH)
        nullUrl shouldBe null
    }
}
