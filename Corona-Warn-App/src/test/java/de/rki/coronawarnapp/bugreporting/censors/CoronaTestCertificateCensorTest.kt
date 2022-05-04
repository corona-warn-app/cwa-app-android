package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.submission.CoronaTestCertificateCensor
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateWrapper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class CoronaTestCertificateCensorTest : BaseTest() {
    @MockK lateinit var coronaTestRepository: TestCertificateRepository

    private val testToken = "63b4d3ff-e0de-4bd4-90c1-17c2bb683a2f"
    private val pcrIdentifier = "qrcode-pcr-someIdentifier"
    private val ratIdentifier = "qrcode-rat-someIdentifier"

    private val coronaTests: MutableStateFlow<Set<TestCertificateWrapper>> = MutableStateFlow(
        setOf(
            mockk<TestCertificateWrapper>().apply {
                every { registrationToken } returns testToken
                every { containerId } returns TestCertificateContainerId(pcrIdentifier)
            },
            mockk<TestCertificateWrapper>().apply {
                every { registrationToken } returns testToken
                every { containerId } returns TestCertificateContainerId(ratIdentifier)
            }
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { coronaTestRepository.certificates } returns coronaTests
    }

    private fun createInstance() = CoronaTestCertificateCensor(
        debugScope = TestScope(),
        coronaTestRepository = coronaTestRepository
    )

    @Test
    fun `censoring replaces the logline message`() = runTest {
        val instance = createInstance()
        val filterMe = "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier"
        instance.checkLog(filterMe)!!
            .compile()!!.censored shouldBe "I'm a shy registration token: ########-####-####-####-########3a2f and we are extrovert qrcode-pcr-CoronaTest/Identifier and qrcode-rat-CoronaTest/Identifier"

        verify { coronaTestRepository.certificates }
    }

    @Test
    fun `censoring returns null if there is no corona test stored`() = runTest {
        coronaTests.value = emptySet()

        val instance = createInstance()
        val filterMeNot =
            "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier"
        instance.checkLog(filterMeNot) shouldBe null
    }

    @Test
    fun `censoring returns null if there is no match`() = runTest {
        val instance = createInstance()
        val filterMeNot = "I'm not a registration token ;)"
        instance.checkLog(filterMeNot) shouldBe null
    }

    @Test
    fun `censoring still works after test was deleted`() = runTest {

        val censor = createInstance()

        val filterMe = "I'm a shy registration token: $testToken and we are extrovert $pcrIdentifier and $ratIdentifier"

        censor.checkLog(filterMe)!!
            .compile()!!.censored shouldBe "I'm a shy registration token: ########-####-####-####-########3a2f and we are extrovert qrcode-pcr-CoronaTest/Identifier and qrcode-rat-CoronaTest/Identifier"

        // delete all tests
        coronaTests.value = emptySet()

        censor.checkLog(filterMe)!!
            .compile()!!.censored shouldBe "I'm a shy registration token: ########-####-####-####-########3a2f and we are extrovert qrcode-pcr-CoronaTest/Identifier and qrcode-rat-CoronaTest/Identifier"
    }
}
