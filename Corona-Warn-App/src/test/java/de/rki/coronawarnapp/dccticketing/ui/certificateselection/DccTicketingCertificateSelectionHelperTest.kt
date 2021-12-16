package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class DccTicketingCertificateSelectionHelperTest : BaseTest() {
    @Test
    fun getFullNameTest() {
        getFullName(null, null) shouldBe ""
        getFullName(null, "First") shouldBe "First"
        getFullName("Last", null) shouldBe "Last"
    }
}
