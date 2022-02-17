package de.rki.coronawarnapp.ccl.holder.grouping

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.dcc.groupByPerson
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

        val result = certificatesList.toSet().groupByPerson()

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

        val result = certificatesList.toSet().groupByPerson()

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

        val result = certificatesList.shuffled().toSet().groupByPerson()

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
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "DR<MARIO",
            lastNameStandardized = "POLO"
        )
    }

    private val certA2: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "POLO",
            lastNameStandardized = "DR<MARIO"
        )
    }

    private val certA3: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "MARIO",
            lastNameStandardized = "POLO"
        )
    }

    private val certA4: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "POLO",
            lastNameStandardized = "MARIO"
        )
    }

    private val certA5: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "MARIO<APPLE",
            lastNameStandardized = "POLO"
        )
    }

    private val certA6: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "APPLE",
            lastNameStandardized = "POLO"
        )
    }

    private val certA7: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "POLO",
            lastNameStandardized = "APPLE"
        )
    }

    private val certA8: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "APPLE<SAM",
            lastNameStandardized = "POLO"
        )
    }

    private val certA9: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "SAM",
            lastNameStandardized = "POLO"
        )
    }

    // Group B
    private val certB1: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "MARK",
            lastNameStandardized = "POLO"
        )
    }

    private val certB2: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "MARK<CARL",
            lastNameStandardized = "POLO"
        )
    }

    private val certB3: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "MARK<CARL<THE<GREAT",
            lastNameStandardized = "POLO"
        )
    }

    // Group C
    private val certC1: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1980-02-03",
            firstNameStandardized = "DJ<ALEX",
            lastNameStandardized = "BOOM"
        )
    }

    // Group D
    private val certD1: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "A",
            lastNameStandardized = "X"
        )
    }

    private val certD2: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "B",
            lastNameStandardized = "X"
        )
    }

    private val certD3: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "C",
            lastNameStandardized = "X"
        )
    }

    private val certD5: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "D",
            lastNameStandardized = "X"
        )
    }

    private val certD6: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "E",
            lastNameStandardized = "X"
        )
    }

    private val certD7: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "F",
            lastNameStandardized = "X"
        )
    }

    private val certD8: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "A B X",
            lastNameStandardized = "X"
        )
    }

    private val certD9: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "C D Y",
            lastNameStandardized = "X"
        )
    }

    private val certD10: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "E F Z",
            lastNameStandardized = "X"
        )
    }

    private val certD11: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "X Y W",
            lastNameStandardized = "X"
        )
    }

    private val certD12: CwaCovidCertificate = mockk {
        every { personIdentifier } returns CertificatePersonIdentifier(
            dateOfBirthFormatted = "1988-01-01",
            firstNameStandardized = "Z W",
            lastNameStandardized = "X"
        )
    }
}
