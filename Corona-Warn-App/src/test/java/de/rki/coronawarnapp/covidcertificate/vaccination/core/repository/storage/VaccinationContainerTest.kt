package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.DefaultValueSet
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Locale
import javax.inject.Inject

class VaccinationContainerTest : BaseTest() {

    @Inject lateinit var testData: VaccinationTestData

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    @Test
    fun `person identifier calculation`() {
        testData.personAVac11Container.personIdentifier shouldBe CertificatePersonIdentifier(
            dateOfBirthFormatted = "1966-11-11",
            firstNameStandardized = "ANDREAS",
            lastNameStandardized = "ASTRA<EINS"
        )
    }

    @Test
    fun `header decoding`() {
        testData.personAVac11Container.header.apply {
            issuer shouldBe "DE"
            issuedAt shouldBe Instant.parse("2021-05-11T09:25:00.000Z")
            expiresAt shouldBe Instant.parse("2022-05-11T09:25:00.000Z")
        }
    }

    @Test
    fun `full property decoding - 1 of 2`() {
        testData.personAVac11Container.apply {
            certificate shouldBe testData.personAVac1Certificate
            qrCodeHash shouldBe testData.personAVac1Container.vaccinationQrCode.toSHA256()
        }
    }

    @Test
    fun `full property decoding - 2 of 2`() {
        testData.personAVac22Container.apply {
            certificate shouldBe testData.personAVac2Certificate
            qrCodeHash shouldBe testData.personAVac2Container.vaccinationQrCode.toSHA256()
        }
    }

    @Test
    fun `mapping to user facing data - valueset is null`() {
        testData.personAVac11Container.toVaccinationCertificate(
            valueSet = null,
            certificateState = CwaCovidCertificate.State.Invalid(),
            userLocale = Locale.GERMAN
        ).apply {
            firstName shouldBe "Andreas"
            lastName shouldBe "Astrá Eins"
            fullName shouldBe "Andreas Astrá Eins"
            dateOfBirthFormatted shouldBe "1966-11-11"
            vaccinatedOnFormatted shouldBe "2021-03-01"
            vaccineTypeName shouldBe "1119305005"
            vaccineManufacturer shouldBe "ORG-100001699"
            medicalProductName shouldBe "EU/1/21/1529"
            doseNumber shouldBe 1
            totalSeriesOfDoses shouldBe 2
            certificateIssuer shouldBe "Bundesministerium für Gesundheit - Test01"
            certificateCountry shouldBe "Deutschland"
            qrCodeHash shouldBe testData.personAVac1Container.vaccinationQrCode.toSHA256()
            uniqueCertificateIdentifier shouldBe
                testData.personAVac11Container.certificate.vaccination.uniqueCertificateIdentifier
            personIdentifier shouldBe CertificatePersonIdentifier(
                dateOfBirthFormatted = "1966-11-11",
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

        testData.personAVac11Container.toVaccinationCertificate(
            valueSet = vaccinationValueSets,
            certificateState = CwaCovidCertificate.State.Invalid(),
            userLocale = Locale.GERMAN,
        ).apply {
            firstName shouldBe "Andreas"
            lastName shouldBe "Astrá Eins"
            fullName shouldBe "Andreas Astrá Eins"
            dateOfBirthFormatted shouldBe "1966-11-11"
            vaccinatedOnFormatted shouldBe "2021-03-01"
            vaccineTypeName shouldBe "Vaccine-Name"
            vaccineManufacturer shouldBe "Manufactorer-Name"
            medicalProductName shouldBe "MedicalProduct-Name"
            doseNumber shouldBe 1
            totalSeriesOfDoses shouldBe 2
            certificateIssuer shouldBe "Bundesministerium für Gesundheit - Test01"
            certificateCountry shouldBe "Deutschland"
            qrCodeHash shouldBe testData.personAVac1Container.vaccinationQrCode.toSHA256()
            uniqueCertificateIdentifier shouldBe
                testData.personAVac11Container.vaccination.uniqueCertificateIdentifier
            personIdentifier shouldBe CertificatePersonIdentifier(
                dateOfBirthFormatted = "1966-11-11",
                firstNameStandardized = "ANDREAS",
                lastNameStandardized = "ASTRA<EINS"
            )
            headerIssuer shouldBe "DE"
            headerIssuedAt shouldBe Instant.parse("2021-05-11T09:25:00.000Z")
            headerExpiresAt shouldBe Instant.parse("2022-05-11T09:25:00.000Z")
        }
    }

    @Test
    fun `nonsense country code appears unchanged`() {
        testData.personXVac1ContainerBadCountryData.toVaccinationCertificate(
            valueSet = null,
            certificateState = CwaCovidCertificate.State.Invalid()
        ).apply {
            certificateCountry shouldBe "YY"
        }
    }

    @Test
    fun `default parsing mode for containers is lenient`() {
        val extractor = mockk<DccQrCodeExtractor>().apply {
            coEvery {
                extract(
                    any(),
                    DccV1Parser.Mode.CERT_VAC_LENIENT
                )
            } returns mockk<VaccinationCertificateQRCode>().apply {
                every { data } returns mockk()
            }
        }

        val container = VaccinationContainer(
            data = StoredVaccinationCertificateData(
                vaccinationQrCode = testData.personYVacTwoEntriesQrCode,
                scannedAt = Instant.EPOCH
            ),
            qrCodeExtractor = extractor
        )

        container.certificateData shouldNotBe null

        coVerify { extractor.extract(testData.personYVacTwoEntriesQrCode, DccV1Parser.Mode.CERT_VAC_LENIENT) }
    }

    @Test
    fun `gracefully handle semi invalid data - multiple entries`() {
        testData.personYVacTwoEntriesContainer.certificate.vaccination
    }
}
