package de.rki.coronawarnapp.ccl.holder.grouping

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.util.dcc.belongToSamePerson
import de.rki.coronawarnapp.util.dcc.sanitizeName
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTestInstrumentation

class DccHolderComparisonTest : BaseTestInstrumentation() {

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(DccHolderComparisonTestCaseProvider::class)
    fun allTestCases(testCase: TestCase) {

        val certA: CwaCovidCertificate = mockk {
            every { dateOfBirthFormatted } returns testCase.holderA.dateOfBirth
            every { sanitizedGivenName } returns testCase.holderA.name.givenName.sanitizeName()
            every { sanitizedFamilyName } returns testCase.holderA.name.familyName.sanitizeName()
        }

        val certB: CwaCovidCertificate = mockk {
            every { dateOfBirthFormatted } returns testCase.holderB.dateOfBirth
            every { sanitizedGivenName } returns testCase.holderB.name.givenName.sanitizeName()
            every { sanitizedFamilyName } returns testCase.holderB.name.familyName.sanitizeName()
        }

        belongToSamePerson(certA, certB) shouldBe testCase.isEqual
    }
}
