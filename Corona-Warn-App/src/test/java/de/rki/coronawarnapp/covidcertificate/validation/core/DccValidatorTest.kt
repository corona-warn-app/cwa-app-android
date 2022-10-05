package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchemaValidator
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.validation.core.business.BusinessValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.business.BusinessValidator
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaValidator
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

@ExperimentalCoroutinesApi
class DccValidatorTest : BaseTest() {

    @Inject lateinit var extractor: DccQrCodeExtractor

    @MockK lateinit var businessValidator: BusinessValidator
    @MockK lateinit var dccJsonSchemaValidator: DccJsonSchemaValidator
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var dscSignatureValidator: DscSignatureValidator

    private lateinit var dccValidator: DccValidator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
        every { dccJsonSchemaValidator.isValid(any()) } returns JsonSchemaValidator.Result(emptySet())
        coEvery { businessValidator.validate(any(), any(), any()) } returns
            BusinessValidation(emptySet(), emptySet())
        every { timeStamper.nowUTC } returns Instant.ofEpochSecond(1625827095)
        coEvery { dscSignatureValidator.validateSignature(any()) } just Runs
        dccValidator = DccValidator(
            businessValidator = businessValidator,
            dccJsonSchemaValidator = dccJsonSchemaValidator,
            dscSignatureValidator = dscSignatureValidator,
            timeStamper = timeStamper
        )
    }

    @Test
    fun `expired certificate fails`() = runTest {
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val validationDateTime = LocalDateTime.parse("2022-06-11T15:00:00")
        val userInput = ValidationUserInput(
            DccCountry("PT"),
            validationDateTime,
        )
        dccValidator.validateDcc(
            userInput,
            certificate.data
        ).expirationCheckPassed shouldBe false
    }

    @Test
    fun `expired certificate fails 2`() = runTest {
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val validationDateTime = LocalDateTime.parse("2022-06-12T11:00:00")
        val userInput = ValidationUserInput(
            DccCountry("PT"),
            validationDateTime,
        )
        dccValidator.validateDcc(
            userInput,
            certificate.data
        ).expirationCheckPassed shouldBe false
    }

    @Test
    fun `valid certificate passes expiration check`() = runTest {
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val validationDateTime = LocalDateTime.parse("2022-06-11T14:00:00")
        val userInput = ValidationUserInput(
            DccCountry("PT"),
            validationDateTime,
        )
        dccValidator.validateDcc(
            userInput,
            certificate.data
        ).expirationCheckPassed shouldBe true
    }

    @Test
    fun `valid certificate passes expiration check 2`() = runTest {
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val validationDateTime = LocalDateTime.parse("2022-06-10T15:00:00")
        val userInput = ValidationUserInput(
            DccCountry("PT"),
            validationDateTime,
        )
        dccValidator.validateDcc(
            userInput,
            certificate.data
        ).expirationCheckPassed shouldBe true
    }
}
