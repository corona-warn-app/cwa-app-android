package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import de.rki.coronawarnapp.util.security.VerificationKeys
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

class WebRequestBuilderTest {
    @MockK
    private lateinit var verificationService: VerificationService

    @MockK
    private lateinit var distributionService: DistributionService

    @MockK
    private lateinit var submissionService: SubmissionService

    @MockK
    private lateinit var verificationKeys: VerificationKeys

    private lateinit var webRequestBuilder: WebRequestBuilder

    @Before
    fun setUp() = run {
        MockKAnnotations.init(this)
        webRequestBuilder = WebRequestBuilder(
            distributionService,
            verificationService,
            submissionService,
            verificationKeys
        )
    }

    @After
    fun tearDown() = unmockkAll()

    @Test
    fun retrievingDateIndexIsSuccessful() {
        val urlString = DiagnosisKeyConstants.AVAILABLE_DATES_URL
        coEvery { distributionService.getDateIndex(urlString) } returns listOf(
            "1900-01-01",
            "2000-01-01"
        )

        runBlocking {
            webRequestBuilder.asyncGetDateIndex("DE") shouldBe listOf(
                LocalDate.parse("1900-01-01"),
                LocalDate.parse("2000-01-01")
            )

            coVerify(exactly = 1) { distributionService.getDateIndex(urlString) }
        }
    }

    @Test
    fun asyncGetHourIndex() {
        val day = Date()
        val urlString = DiagnosisKeyConstants.AVAILABLE_DATES_URL +
                "/${day.toServerFormat()}/${DiagnosisKeyConstants.HOUR}"

        coEvery { distributionService.getHourIndex(urlString) } returns listOf("1", "2")

        runBlocking {
            webRequestBuilder.asyncGetHourIndex(day) shouldBe listOf(
                LocalTime.parse("01:00"),
                LocalTime.parse("02:00")
            )

            coVerify(exactly = 1) { distributionService.getHourIndex(urlString) }
        }
    }
}
