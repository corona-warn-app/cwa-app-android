package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.DaggerVaccinationTestComponent
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.DefaultValueSet
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Locale
import javax.inject.Inject

class VaccinationContainerTest : BaseTest() {

    @Inject lateinit var testData: VaccinationTestData

    @BeforeEach
    fun setup() {
        DaggerVaccinationTestComponent.factory().create().inject(this)
    }

    @Test
    fun `person identifier calculation`() {
        testData.personAVac1Container.personIdentifier shouldBe CertificatePersonIdentifier(
            dateOfBirth = LocalDate.parse("1966-11-11"),
            firstNameStandardized = "ANDREAS",
            lastNameStandardized = "ASTRA<EINS"
        )
    }

    @Test
    fun `header decoding`() {
        testData.personAVac1Container.header.apply {
            issuer shouldBe "DE"
            issuedAt shouldBe Instant.parse("2021-05-11T09:25:00.000Z")
            expiresAt shouldBe Instant.parse("2022-05-11T09:25:00.000Z")
        }
    }

    @Test
    fun `full property decoding - 1 of 2`() {
        testData.personAVac1Container.apply {
            certificate shouldBe testData.personAVac1Certificate
            certificateId shouldBe "01DE/00001/1119305005/7T1UG87G61Y7NRXIBQJDTYQ9#S"
        }
    }

    @Test
    fun `full property decoding - 2 of 2`() {
        testData.personAVac2Container.apply {
            certificate shouldBe testData.personAVac2Certificate
            certificateId shouldBe "01DE/00001/1119305005/6IPYBAIDWEWRWW73QEP92FQSN#S"
        }
    }

    @Test
    fun `mapping to user facing data - valueset is null`() {
        testData.personAVac1Container.toVaccinationCertificate(null, userLocale = Locale.GERMAN).apply {
            firstName shouldBe "Andreas"
            lastName shouldBe "Astrá Eins"
            fullName shouldBe "Andreas Astrá Eins"
            dateOfBirth shouldBe LocalDate.parse("1966-11-11")
            vaccinatedAt shouldBe LocalDate.parse("2021-03-01")
            vaccineTypeName shouldBe "1119305005"
            vaccineManufacturer shouldBe "ORG-100001699"
            medicalProductName shouldBe "EU/1/21/1529"
            doseNumber shouldBe 1
            totalSeriesOfDoses shouldBe 2
            certificateIssuer shouldBe "Bundesministerium für Gesundheit - Test01"
            certificateCountry shouldBe "Deutschland"
            certificateId shouldBe "01DE/00001/1119305005/7T1UG87G61Y7NRXIBQJDTYQ9#S"
            personIdentifier shouldBe CertificatePersonIdentifier(
                dateOfBirth = LocalDate.parse("1966-11-11"),
                firstNameStandardized = "ANDREAS",
                lastNameStandardized = "ASTRA<EINS"
            )
        }
    }

    @Test
    fun `mapping to user facing data - with valueset`() {
        val vpItem = DefaultValueSet.DefaultItem(
            key = "1119305005",
            displayText = "Vaccine-Name"
        )

        val mpItem = DefaultValueSet.DefaultItem(
            key = "EU/1/21/1529",
            displayText = "MedicalProduct-Name"
        )

        val maItem = DefaultValueSet.DefaultItem(
            key = "ORG-100001699",
            displayText = "Manufactorer-Name"
        )

        val vaccinationValueSets = VaccinationValueSets(
            languageCode = Locale.GERMAN,
            tg = DefaultValueSet(),
            vp = DefaultValueSet(items = listOf(vpItem)),
            mp = DefaultValueSet(items = listOf(mpItem)),
            ma = DefaultValueSet(items = listOf(maItem))
        )

        testData.personAVac1Container.toVaccinationCertificate(vaccinationValueSets, userLocale = Locale.GERMAN).apply {
            firstName shouldBe "Andreas"
            lastName shouldBe "Astrá Eins"
            fullName shouldBe "Andreas Astrá Eins"
            dateOfBirth shouldBe LocalDate.parse("1966-11-11")
            vaccinatedAt shouldBe LocalDate.parse("2021-03-01")
            vaccineTypeName shouldBe "Vaccine-Name"
            vaccineManufacturer shouldBe "Manufactorer-Name"
            medicalProductName shouldBe "MedicalProduct-Name"
            doseNumber shouldBe 1
            totalSeriesOfDoses shouldBe 2
            certificateIssuer shouldBe "Bundesministerium für Gesundheit - Test01"
            certificateCountry shouldBe "Deutschland"
            certificateId shouldBe "01DE/00001/1119305005/7T1UG87G61Y7NRXIBQJDTYQ9#S"
            personIdentifier shouldBe CertificatePersonIdentifier(
                dateOfBirth = LocalDate.parse("1966-11-11"),
                firstNameStandardized = "ANDREAS",
                lastNameStandardized = "ASTRA<EINS"
            )
            issuer shouldBe "DE"
            issuedAt shouldBe Instant.parse("2021-05-11T09:25:00.000Z")
            expiresAt shouldBe Instant.parse("2022-05-11T09:25:00.000Z")
        }
    }

    @Test
    fun `nonsense country code appears unchanged`() {
        testData.personXVac1ContainerBadCountryData.toVaccinationCertificate(null).apply {
            certificateCountry shouldBe "YY"
        }
    }

    @Test
    fun `default parsing mode for containers is lenient`() {
        val container = VaccinationContainer(
            vaccinationQrCode = testData.personYVacTwoEntriesQrCode,
            scannedAt = Instant.EPOCH
        )
        val extractor = mockk<DccQrCodeExtractor>().apply {
            every { extract(any(), any()) } returns mockk<VaccinationCertificateQRCode>().apply {
                every { data } returns mockk()
            }
        }
        container.qrCodeExtractor = extractor

        container.certificateData shouldNotBe null

        verify { extractor.extract(testData.personYVacTwoEntriesQrCode, QrCodeExtractor.Mode.CERT_VAC_LENIENT) }
    }

    @Test
    fun `gracefully handle semi invalid data - multiple entries`() {
        testData.personYVacTwoEntriesContainer.certificate.payloads.size shouldBe 1
    }
}
