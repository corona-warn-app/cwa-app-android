package de.rki.coronawarnapp.ccl.holder.grouping

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.dcc.group2
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTestInstrumentation

class DccGroupingExtensionsTest : BaseTestInstrumentation() {

    @Test
    fun `grouping check`() {
        val certificatesList = setOf(
            cert_1_1,
            cert_1_2,
            cert_1_3,
            cert_1_4,
            cert_1_5,
            cert_1_6,
            cert_1_7,
            cert_1_9,
            cert_1_8,
            cert_2_1,
            cert_2_2,
            cert_2_3,
            cert_3_1
        )

        val result = certificatesList.shuffled().toSet().group2()

        result.count() shouldBe 3

        result[0].containsAll(listOf(
            cert_1_1,
            cert_1_2,
            cert_1_3,
            cert_1_4,
            cert_1_5,
            cert_1_6,
            cert_1_7,
            cert_1_8,
            cert_1_9,
        )) shouldBe true

        result[1].containsAll(
            listOf(
                cert_2_1,
                cert_2_2,
                cert_2_3
            )
        ) shouldBe true

        result[2].containsAll(
            listOf(
                cert_3_1
            )
        ) shouldBe true
    }

    // Group 1
    private val cert_1_1: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "DR<MARIO"
        every { lastName } returns "POLO"
    }

    private val cert_1_2: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "POLO"
        every { lastName } returns "DR<MARIO"
    }

    private val cert_1_3: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "POLO"
        every { lastName } returns "MARIO"
    }

    private val cert_1_4: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "MARIO"
        every { lastName } returns "POLO"
    }

    private val cert_1_5: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "MARIO<APPLE"
        every { lastName } returns "POLO"
    }

    private val cert_1_6: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "APPLE"
        every { lastName } returns "POLO"
    }

    private val cert_1_7: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "POLO"
        every { lastName } returns "APPLE"
    }

    private val cert_1_8: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "APPLE<SAM"
        every { lastName } returns "POLO"
    }

    private val cert_1_9: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "SAM"
        every { lastName } returns "POLO"
    }

    // Group 2
    private val cert_2_1: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "MARK"
        every { lastName } returns "POLO"
    }

    private val cert_2_2: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "MARK<CARL"
        every { lastName } returns "POLO"
    }

    private val cert_2_3: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "MARK<CARL<THE<GREAT"
        every { lastName } returns "POLO"
    }

    // Group 3
    private val cert_3_1: CwaCovidCertificate = mockk {
        every { dateOfBirthFormatted } returns "1980-02-03"
        every { firstName } returns "DJ<ALEX"
        every { lastName } returns "BOOM"
    }
}
