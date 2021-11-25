package de.rki.coronawarnapp.dccticketing.ui.consent.one

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class DccTicketingConsentOneViewModelTest : BaseTest() {

    @MockK private lateinit var dccTicketingSharedViewModel: DccTicketingSharedViewModel
    @MockK private lateinit var dccTicketingConsentOneProcessor: DccTicketingConsentOneProcessor

    private val mutableTransactionContextFlow = MutableStateFlow<DccTicketingTransactionContext?>(null)

    private val instance: DccTicketingConsentOneViewModel
        get() = DccTicketingConsentOneViewModel(
            dccTicketingSharedViewModel = dccTicketingSharedViewModel,
            dispatcherProvider = TestDispatcherProvider(),
            dccTicketingConsentOneProcessor = dccTicketingConsentOneProcessor
        )

    private val data = DccTicketingQrCodeData(
        protocol = "Test protocol",
        protocolVersion = "Test protocolVersion",
        serviceIdentity = "Test serviceIdentity",
        privacyUrl = "https://www.test.de/",
        token = "Test token",
        consent = "Test consent",
        subject = "Test subject",
        serviceProvider = "Test serviceProvider"
    )

    private val transactionContext = DccTicketingTransactionContext(initializationData = data)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mutableTransactionContextFlow.value = transactionContext
        dccTicketingSharedViewModel.apply {
            every { transactionContext } returns mutableTransactionContextFlow.filterNotNull()
            every { updateTransactionContext(any()) } answers { mutableTransactionContextFlow.value = arg(0) }
        }
    }

    @Test
    fun `UiState mapping`() {
        instance.uiState.getOrAwaitValue().run {
            provider shouldBe data.serviceProvider
            subject shouldBe data.subject
        }
    }

    @Test
    fun `onConsent() - happy path`() = runBlockingTest2(ignoreActive = true) {
        val updatedTransactionContext = transactionContext.copy(
            initializationData = transactionContext.initializationData.copy(
                protocol = "Test protocol updated"
            )
        )

        coEvery { dccTicketingConsentOneProcessor.processUserConsent(any()) } returns updatedTransactionContext

        instance.run {
            onUserConsent()

            events.getOrAwaitValue() shouldBe NavigateToCertificateSelection
        }

        mutableTransactionContextFlow.value shouldBe updatedTransactionContext
    }

    @Test
    fun `onConsent() - error path`() {
        coEvery { dccTicketingConsentOneProcessor.processUserConsent(any()) } throws DccTicketingException(
            errorCode = DccTicketingException.ErrorCode.VS_ID_CERT_PIN_NO_JWK_FOR_KID
        )

        instance.run {
            onUserConsent()
            events.getOrAwaitValue() should beInstanceOf<ShowErrorDialog>()
        }

        mutableTransactionContextFlow.value = transactionContext
    }

    @Test
    fun `goBack() - NavigateBack`() {
        instance.run {
            goBack()
            events.getOrAwaitValue() shouldBe NavigateBack
        }
    }

    @Test
    fun `onUserCancel() - ShowCancelConfirmationDialog`() {
        instance.run {
            onUserCancel()
            events.getOrAwaitValue() shouldBe ShowCancelConfirmationDialog
        }
    }

    @Test
    fun `showPrivacyInformation() - NavigateToPrivacyInformation`() {
        instance.run {
            showPrivacyInformation()
            events.getOrAwaitValue() shouldBe NavigateToPrivacyInformation
        }
    }
}
