package de.rki.coronawarnapp.ccl.holder.grouping

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
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
            every { personIdentifier } returns CertificatePersonIdentifier(
                dateOfBirthFormatted = testCase.holderA.dateOfBirth,
                firstNameStandardized = testCase.holderA.name.givenName,
                lastNameStandardized = testCase.holderA.name.familyName
            )
        }

        val certB: CwaCovidCertificate = mockk {
            every { personIdentifier } returns CertificatePersonIdentifier(
                dateOfBirthFormatted = testCase.holderB.dateOfBirth,
                firstNameStandardized = testCase.holderB.name.givenName,
                lastNameStandardized = testCase.holderB.name.familyName
            )
        }

        certA.personIdentifier.isTheSamePerson(certB) shouldBe testCase.isEqual
    }
}
