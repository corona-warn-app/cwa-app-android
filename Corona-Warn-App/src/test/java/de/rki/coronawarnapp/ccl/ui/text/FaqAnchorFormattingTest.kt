package de.rki.coronawarnapp.ccl.ui.text

import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CCLJsonFunctions
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Locale

internal class FaqAnchorFormattingTest : BaseTest() {
    @MockK private lateinit var cclJsonFunctions: CCLJsonFunctions
    private val mapper = SerializationModule.jacksonBaseMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun formatFaqAnchor() {
        val format = CCLTextFormatter(cclJsonFunctions, mapper)
        val deUrl = format("dcc_admission_state", Locale.GERMAN)
        deUrl shouldBe "https://www.coronawarn.app/de/faq/#dcc_admission_state"

        val enUrl = format("dcc_admission_state", Locale.FRENCH)
        enUrl shouldBe "https://www.coronawarn.app/en/faq/#dcc_admission_state"

        val url = format("dcc_admission_state", Locale.ENGLISH)
        url shouldBe "https://www.coronawarn.app/en/faq/#dcc_admission_state"

        val anchor: String? = null
        val nullUrl = format(anchor, Locale.ENGLISH)
        nullUrl shouldBe null
    }
}
