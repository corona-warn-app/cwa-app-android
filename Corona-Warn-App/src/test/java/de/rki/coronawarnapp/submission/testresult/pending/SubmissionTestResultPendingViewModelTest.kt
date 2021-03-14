package de.rki.coronawarnapp.submission.testresult.pending

import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.ui.submission.testresult.pending.SubmissionTestResultPendingViewModel
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.asDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionTestResultPendingViewModelTest : BaseTest() {

    @MockK lateinit var shareTestResultNotificationService: ShareTestResultNotificationService
    @MockK lateinit var submissionRepository: SubmissionRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        submissionRepository.apply {
            every { hasGivenConsentToSubmission } returns emptyFlow()
            every { deviceUIStateFlow } returns emptyFlow()
            every { testResultReceivedDateFlow } returns emptyFlow()
        }
    }

    fun createInstance(scope: CoroutineScope = TestCoroutineScope()) = SubmissionTestResultPendingViewModel(
        dispatcherProvider = scope.asDispatcherProvider(),
        shareTestResultNotificationService = shareTestResultNotificationService,
        submissionRepository = submissionRepository
    )

    @Test
    fun `web exception handling`() {
        val expectedType = NetworkRequestWrapper.RequestFailed<DeviceUIState, CwaWebException>(
            CwaWebException(statusCode = 1, message = "message")
        )
        val unexpectedType =
            NetworkRequestWrapper.RequestFailed<DeviceUIState, Throwable>(UnsupportedOperationException())
        val deviceUI = MutableStateFlow<NetworkRequestWrapper<DeviceUIState, Throwable>>(expectedType)
        every { submissionRepository.deviceUIStateFlow } returns deviceUI
        createInstance().apply {
            cwaWebExceptionLiveData.observeForever {}
            cwaWebExceptionLiveData.value shouldBe expectedType.error

            deviceUI.value = unexpectedType
            cwaWebExceptionLiveData.value shouldBe unexpectedType.error
        }
    }
}
