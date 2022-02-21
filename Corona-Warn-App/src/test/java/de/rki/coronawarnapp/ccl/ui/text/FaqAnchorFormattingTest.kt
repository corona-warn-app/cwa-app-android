package de.rki.coronawarnapp.ccl.ui.text

import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CclJsonFunctions
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Locale

internal class FaqAnchorFormattingTest : BaseTest() {
    @MockK private lateinit var cclJsonFunctions: CclJsonFunctions
    private val mapper = SerializationModule.jacksonBaseMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun formatFaqAnchor() {
        val format = CclTextFormatter(cclJsonFunctions, mapper)
        val deUrl = format(faqAnchor = "dcc_admission_state", "de")
        deUrl shouldBe "https://www.coronawarn.app/de/faq/#dcc_admission_state"

        val enUrl = format(faqAnchor = "dcc_admission_state", "ro")
        enUrl shouldBe "https://www.coronawarn.app/en/faq/#dcc_admission_state"

        val url = format(faqAnchor = "dcc_admission_state", "en")
        url shouldBe "https://www.coronawarn.app/en/faq/#dcc_admission_state"

        val anchor: String? = null
        val nullUrl = format(anchor, Locale.ENGLISH.language)
        nullUrl shouldBe null
    }
}
