package de.rki.coronawarnapp.ccl.dccwalletinfo

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PersonWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class DccWalletInfoCleanerTest : BaseTest() {

    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var dccWalletInfoRepository: DccWalletInfoRepository

    private val personIdentifier1 = CertificatePersonIdentifier(
        firstNameStandardized = "A",
        lastNameStandardized = "AA",
        dateOfBirthFormatted = "01.01.2020"
    )

    private val person1 = mockk<PersonCertificates>().apply {
        every { personIdentifier } returns personIdentifier1
    }

    private val personIdentifier2 = CertificatePersonIdentifier(
        firstNameStandardized = "B",
        lastNameStandardized = "BB",
        dateOfBirthFormatted = "02.01.2020"
    )

    private val person2 = mockk<PersonCertificates>().apply {
        every { personIdentifier } returns personIdentifier2
    }

    private val personIdentifier3 = CertificatePersonIdentifier(
        firstNameStandardized = "C",
        lastNameStandardized = "CC",
        dateOfBirthFormatted = "03.01.2020"
    )

    private val person3 = mockk<PersonCertificates>().apply {
        every { personIdentifier } returns personIdentifier3
    }

    private val personIdentifier4 = CertificatePersonIdentifier(
        firstNameStandardized = "D",
        lastNameStandardized = "DD",
        dateOfBirthFormatted = "04.01.2020"
    )

    private val person4 = mockk<PersonCertificates>().apply {
        every { personIdentifier } returns personIdentifier4
    }

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { personCertificatesProvider.personCertificates } returns flowOf(
            setOf(
                person1,
                person2,
                person3
            )
        )

        every { dccWalletInfoRepository.personWallets } returns flowOf(
            setOf(
                PersonWalletInfo(person1.personIdentifier.groupingKey, null),
                PersonWalletInfo(person2.personIdentifier.groupingKey, null),
                PersonWalletInfo(person4.personIdentifier.groupingKey, null),
            )
        )
        coEvery { dccWalletInfoRepository.delete(setOf("04.01.2020#DD#D")) } just Runs
    }

    @Test
    fun clean() = runTest {
        DccWalletInfoCleaner(
            personCertificatesProvider,
            dccWalletInfoRepository
        ).clean()

        coVerify {
            dccWalletInfoRepository.delete(setOf("04.01.2020#DD#D"))
        }
    }
}
