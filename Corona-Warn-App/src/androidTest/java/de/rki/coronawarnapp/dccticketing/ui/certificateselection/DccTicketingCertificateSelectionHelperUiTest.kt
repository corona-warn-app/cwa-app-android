package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.R
import io.kotest.matchers.shouldBe
import org.junit.Test
import testhelpers.BaseTestInstrumentation

class DccTicketingCertificateSelectionHelperUiTest : BaseTestInstrumentation() {

    private val context by lazy { ApplicationProvider.getApplicationContext<Context>() }

    @Test
    fun certificateTypesText() = with(context) {
        certificateTypesText(listOf("v", "r", "t", "tp", "tr")) shouldBe
            listOf(
                getString(R.string.vaccination_certificate_name),
                getString(R.string.recovery_certificate_name),
                getString(R.string.rat_test_certificate),
                getString(R.string.pcr_test_certificate)
            ).joinToString(", ")

        certificateTypesText(listOf("v", "r", "tp")) shouldBe
            listOf(
                getString(R.string.vaccination_certificate_name),
                getString(R.string.recovery_certificate_name),
                getString(R.string.pcr_test_certificate)
            ).joinToString(", ")

        certificateTypesText(listOf("v", "r", "tr")) shouldBe
            listOf(
                getString(R.string.vaccination_certificate_name),
                getString(R.string.recovery_certificate_name),
                getString(R.string.rat_test_certificate)
            ).joinToString(", ")

        certificateTypesText(listOf("v", "r")) shouldBe
            listOf(
                getString(R.string.vaccination_certificate_name),
                getString(R.string.recovery_certificate_name)
            ).joinToString(", ")

        certificateTypesText(listOf("r")) shouldBe
            listOf(
                getString(R.string.recovery_certificate_name)
            ).joinToString(", ")

        certificateTypesText(listOf("v")) shouldBe
            listOf(
                getString(R.string.vaccination_certificate_name)
            ).joinToString(", ")

        certificateTypesText(listOf("v", "r", "tp", "tr")) shouldBe
            listOf(
                getString(R.string.vaccination_certificate_name),
                getString(R.string.recovery_certificate_name),
                getString(R.string.pcr_test_certificate),
                getString(R.string.rat_test_certificate)
            ).joinToString(", ")
    }
}
