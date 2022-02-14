package de.rki.coronawarnapp.ccl.holder.grouping

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.dcc.groupCertificatesByPerson
import de.rki.coronawarnapp.util.dcc.sanitizeName
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTestInstrumentation

class DccGroupingExtensionsTest : BaseTestInstrumentation() {

    @Test
    fun `first grouping check`() {
        val certificatesList = setOf(
            certA1,
            certA2,
            certA3,
            certA4,
            certA5,
            certA6,
            certA7,
            certA9,
            certA8,
            certB1,
            certB2,
            certB3,
            certC1
        )

        val result = certificatesList.toSet().groupCertificatesByPerson()

        result.count() shouldBe 3

        result[0] shouldContainAll listOf(
            certA1,
            certA2,
            certA3,
            certA4,
            certA5,
            certA6,
            certA7,
            certA8,
            certA9,
        )

        result[1] shouldContainAll listOf(
            certB1,
            certB2,
            certB3
        )

        result[2] shouldContainAll listOf(
            certC1
        )
    }

    @Test
    fun `empty grouping check`() {
        val certificatesList = emptySet<CwaCovidCertificate>()

        val result = certificatesList.toSet().groupCertificatesByPerson()

        result.count() shouldBe 0
    }

    @Test
    fun `second grouping check`() {
        val certificatesList = setOf(
            certD12,
            certD11,
            certD10,
            certD9,
            certD8,
            certD7,
            certD6,
            certD5,
            certD3,
            certD2,
            certD1,
        )

        val result = certificatesList.shuffled().toSet().groupCertificatesByPerson()

        result.count() shouldBe 1

        result[0] shouldContainAll listOf(
            certD12,
            certD11,
            certD10,
            certD9,
            certD8,
            certD7,
            certD6,
            certD5,
            certD3,
            certD2,
            certD1,
        )
    }

    // Group A
    private val certA1: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "DR<MARIO".sanitizeName()
        every { sanitizedFamilyName } returns "POLO".sanitizeName()
    }

    private val certA2: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "POLO".sanitizeName()
        every { sanitizedFamilyName } returns "DR<MARIO".sanitizeName()
    }

    private val certA3: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "POLO".sanitizeName()
        every { sanitizedFamilyName } returns "MARIO".sanitizeName()
    }

    private val certA4: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "MARIO".sanitizeName()
        every { sanitizedFamilyName } returns "POLO".sanitizeName()
    }

    private val certA5: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "MARIO<APPLE".sanitizeName()
        every { sanitizedFamilyName } returns "POLO".sanitizeName()
    }

    private val certA6: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "APPLE".sanitizeName()
        every { sanitizedFamilyName } returns "POLO".sanitizeName()
    }

    private val certA7: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "POLO".sanitizeName()
        every { sanitizedFamilyName } returns "APPLE".sanitizeName()
    }

    private val certA8: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "APPLE<SAM".sanitizeName()
        every { sanitizedFamilyName } returns "POLO".sanitizeName()
    }

    private val certA9: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "SAM".sanitizeName()
        every { sanitizedFamilyName } returns "POLO".sanitizeName()
    }

    // Group B
    private val certB1: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "MARK".sanitizeName()
        every { sanitizedFamilyName } returns "POLO".sanitizeName()
    }

    private val certB2: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "MARK<CARL".sanitizeName()
        every { sanitizedFamilyName } returns "POLO".sanitizeName()
    }

    private val certB3: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "MARK<CARL<THE<GREAT".sanitizeName()
        every { sanitizedFamilyName } returns "POLO".sanitizeName()
    }

    // Group C
    private val certC1: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { sanitizedGivenName } returns "DJ<ALEX".sanitizeName()
        every { sanitizedFamilyName } returns "BOOM".sanitizeName()
    }

    // Group D
    private val certD1: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "A".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }

    private val certD2: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "B".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }

    private val certD3: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "C".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }

    private val certD5: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "D".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }

    private val certD6: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "E".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }

    private val certD7: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "F".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }

    private val certD8: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "A B X".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }

    private val certD9: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "C D Y".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }

    private val certD10: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "E F Z".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }

    private val certD11: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "X Y W".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }

    private val certD12: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1988-01-01"
        every { sanitizedGivenName } returns "Z W".sanitizeName()
        every { sanitizedFamilyName } returns "X".sanitizeName()
    }
}
