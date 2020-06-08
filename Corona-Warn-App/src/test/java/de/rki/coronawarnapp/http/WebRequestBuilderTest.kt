package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.http.service.DistributionService
import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toServerFormat
import de.rki.coronawarnapp.util.security.VerificationKeys
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Date

class WebRequestBuilderTest {
    @MockK private lateinit var verificationKeys: VerificationKeys
    @MockK private lateinit var verificationService: VerificationService
    @MockK private lateinit var distributionService: DistributionService
    @MockK private lateinit var submissionService: SubmissionService
    @MockK private lateinit var serviceFactory: ServiceFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(WebRequestBuilder)
        every { WebRequestBuilder["verificationKeys"]() } returns verificationKeys
        every { WebRequestBuilder["serviceFactory"]() } returns serviceFactory
        every { serviceFactory.distributionService() } returns distributionService
        every { serviceFactory.verificationService() } returns verificationService
        every { serviceFactory.submissionService() } returns submissionService
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun retrievingDateIndexIsSuccessful() {
        val urlString = DiagnosisKeyConstants.AVAILABLE_DATES_URL
        coEvery { distributionService.getDateIndex(urlString) }
            .returns(listOf("1900-01-01", "2000-01-01"))

        runBlocking {
            val expectedResult = listOf("1900-01-01", "2000-01-01")
            Assert.assertEquals(WebRequestBuilder.asyncGetDateIndex(), expectedResult)
            coVerify {
                distributionService.getDateIndex(urlString)
            }
        }
    }

    @Test
    fun asyncGetHourIndex() {
        val day = Date()
        val urlString = DiagnosisKeyConstants.AVAILABLE_DATES_URL +
                "/${day.toServerFormat()}/${DiagnosisKeyConstants.HOUR}"

        coEvery { distributionService.getHourIndex(urlString) }
            .returns(listOf("1", "2"))

        runBlocking {
            val expectedResult = listOf("1", "2")
            Assert.assertEquals(WebRequestBuilder.asyncGetHourIndex(day), expectedResult)
            coVerify {
                distributionService.getHourIndex(urlString)
            }
        }
    }
}