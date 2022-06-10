package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.content.Context
import android.content.res.AssetManager
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.ByteArrayInputStream

internal class CertificateTemplateTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var assetManager: AssetManager

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.assets } returns assetManager

        every { assetManager.open(any()) } answers {
            when (args[0].toString()) {
                "template/de_vc_v4.1.svg" -> "DE-VC-Template"
                "template/de_rc_v4.1.svg" -> "DE-RC-Template"
                "template/de_tc_v4.1.svg" -> "DE-TC-Template"
                "template/vc_v4.1.svg" -> "VC-Template"
                "template/rc_v4.1.svg" -> "RC-Template"
                "template/tc_v4.1.svg" -> "TC-Template"
                else -> "null"
            }.run {
                ByteArrayInputStream(toByteArray())
            }
        }
    }

    @Test
    fun `vc-templates`() {
        val template = CertificateTemplate(context)
        val dcVc = mockk<VaccinationCertificate>().apply {
            every { headerIssuer } returns "DE"
        }
        template(dcVc)
        template(dcVc) shouldBe "DE-VC-Template" // To make sure template is loaded once

        val vc = mockk<VaccinationCertificate>().apply {
            every { headerIssuer } returns "AT"
        }
        template(vc)
        template(vc) shouldBe "VC-Template" // To make sure template is loaded once

        verify(exactly = 1) {
            assetManager.open("template/de_vc_v4.1.svg")
            assetManager.open("template/vc_v4.1.svg")
        }
    }

    @Test
    fun `tc-templates`() {
        val template = CertificateTemplate(context)
        val dcTc = mockk<TestCertificate>().apply {
            every { headerIssuer } returns "DE"
        }
        template(dcTc)
        template(dcTc) shouldBe "DE-TC-Template" // To make sure template is loaded once

        val tc = mockk<TestCertificate>().apply {
            every { headerIssuer } returns "AT"
        }
        template(tc)
        template(tc) shouldBe "TC-Template" // To make sure template is loaded once

        verify(exactly = 1) {
            assetManager.open("template/de_tc_v4.1.svg")
            assetManager.open("template/tc_v4.1.svg")
        }
    }

    @Test
    fun `rc-templates`() {
        val template = CertificateTemplate(context)
        val dcRc = mockk<RecoveryCertificate>().apply {
            every { headerIssuer } returns "DE"
        }
        template(dcRc)
        template(dcRc) shouldBe "DE-RC-Template" // To make sure template is loaded once

        val rc = mockk<RecoveryCertificate>().apply {
            every { headerIssuer } returns "AT"
        }
        template(rc)
        template(rc) shouldBe "RC-Template" // To make sure template is loaded once

        verify(exactly = 1) {
            assetManager.open("template/de_rc_v4.1.svg")
            assetManager.open("template/rc_v4.1.svg")
        }
    }
}
