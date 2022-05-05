package de.rki.coronawarnapp.familytest.ui.consent

import de.rki.coronawarnapp.bugreporting.censors.family.FamilyTestCensor
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class FamilyTestConsentViewModelTest : BaseTest() {

    @MockK lateinit var testRegistrationStateProcessor: TestRegistrationStateProcessor
    @MockK lateinit var familyTestCensor: FamilyTestCensor

    private lateinit var viewModelDcc: FamilyTestConsentViewModel

    private val qrDcc = CoronaTestQRCode.RapidAntigen(
        isDccSupportedByPoc = true,
        createdAt = Instant.now(), hash = "", rawQrCode = ""
    )
    private val qrNoDcc = CoronaTestQRCode.RapidAntigen(
        isDccSupportedByPoc = false,
        createdAt = Instant.now(), hash = "", rawQrCode = ""
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        testRegistrationStateProcessor.apply {
            every { state } returns flowOf(TestRegistrationStateProcessor.State.Idle)
            coEvery { startTestRegistration(any(), any(), any()) } returns mockk()
        }

        familyTestCensor.apply {
            coEvery { addName(any()) } returns Unit
        }

        viewModelDcc = FamilyTestConsentViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            coronaTestQRCode = qrDcc,
            familyTestCensor = familyTestCensor,
            registrationStateProcessor = testRegistrationStateProcessor
        )
    }

    @Test
    fun testNameChanged() {
        viewModelDcc.nameChanged("My Name")
        viewModelDcc.isSubmittable.value shouldBe true
    }

    @Test
    fun testNameNotChanged() {
        viewModelDcc.isSubmittable.value shouldBe false
    }

    @Test
    fun `onConsentButtonClick with DCC returns Navigation Event`() {
        viewModelDcc.nameChanged("My Name")
        viewModelDcc.onConsentButtonClick()

        Assert.assertTrue(
            viewModelDcc.routeToScreen.value is FamilyTestConsentNavigationEvents.NavigateToCertificateRequest
        )
    }

    @Test
    fun `onConsentButtonClick without DCC starts test registration`() {

        val viewModelNoDcc = FamilyTestConsentViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            coronaTestQRCode = qrNoDcc,
            familyTestCensor = familyTestCensor,
            registrationStateProcessor = testRegistrationStateProcessor
        )

        viewModelNoDcc.nameChanged("My Name")
        viewModelNoDcc.onConsentButtonClick()

        coVerify(exactly = 1) {
            testRegistrationStateProcessor.startFamilyTestRegistration(
                request = qrNoDcc,
                personName = "My Name"
            )
        }
    }

    @Test
    fun testOnDataPrivacyClick() {
        viewModelDcc.onDataPrivacyClick()
        viewModelDcc.routeToScreen.value shouldBe FamilyTestConsentNavigationEvents.NavigateToDataPrivacy
    }

    @Test
    fun testOnNavigateBack() {
        viewModelDcc.onNavigateBack()
        viewModelDcc.routeToScreen.value shouldBe FamilyTestConsentNavigationEvents.NavigateBack
    }
}
