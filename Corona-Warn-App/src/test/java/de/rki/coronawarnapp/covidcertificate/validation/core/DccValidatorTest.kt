package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchemaValidator
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.validation.core.business.BusinessValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.business.BusinessValidator
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaValidator
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class DccValidatorTest : BaseTest() {

    @Inject lateinit var extractor: DccQrCodeExtractor

    @MockK lateinit var businessValidator: BusinessValidator
    @MockK lateinit var dccJsonSchemaValidator: DccJsonSchemaValidator
    @MockK lateinit var timeStamper: TimeStamper

    private lateinit var dccValidator: DccValidator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
        every { dccJsonSchemaValidator.isValid(any()) } returns JsonSchemaValidator.Result(emptySet())
        coEvery { businessValidator.validate(any(), any(), any(), any()) } returns
            BusinessValidation(emptySet(), emptySet())
        every { timeStamper.nowUTC } returns Instant.ofEpochSecond(1625827095)
        dccValidator = DccValidator(businessValidator, dccJsonSchemaValidator, timeStamper)
    }

    @Test
    fun `expired certificate fails`() = runBlockingTest {
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val validationDate = LocalDate.parse("2022-06-11")
        val validationTime = LocalTime.parse("15:00:00")
        val userInput = ValidationUserInput(
            DccCountry("PT"),
            validationDate,
            validationTime,
        )
        dccValidator.validateDcc(
            userInput,
            certificate.data
        ).expirationCheckPassed shouldBe false
    }

    @Test
    fun `expired certificate fails 2`() = runBlockingTest {
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val validationDate = LocalDate.parse("2022-06-12")
        val validationTime = LocalTime.parse("11:00:00")
        val userInput = ValidationUserInput(
            DccCountry("PT"),
            validationDate,
            validationTime,
        )
        dccValidator.validateDcc(
            userInput,
            certificate.data
        ).expirationCheckPassed shouldBe false
    }

    @Test
    fun `valid certificate passes expiration check`() = runBlockingTest {
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val validationDate = LocalDate.parse("2022-06-11")
        val validationTime = LocalTime.parse("14:00:00")
        val userInput = ValidationUserInput(
            DccCountry("PT"),
            validationDate,
            validationTime,
        )
        dccValidator.validateDcc(
            userInput,
            certificate.data
        ).expirationCheckPassed shouldBe true
    }

    @Test
    fun `valid certificate passes expiration check 2`() = runBlockingTest {
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val validationDate = LocalDate.parse("2022-06-10")
        val validationTime = LocalTime.parse("15:00:00")
        val userInput = ValidationUserInput(
            DccCountry("PT"),
            validationDate,
            validationTime,
        )
        dccValidator.validateDcc(
            userInput,
            certificate.data
        ).expirationCheckPassed shouldBe true
    }
}
