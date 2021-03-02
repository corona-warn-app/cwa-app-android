package de.rki.coronawarnapp.tracing.ui.homecards

import android.content.Context
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.ui.homecards.NoTest
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionStateProvider
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.verifySequence
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.preferences.mockFlowPreference
import java.util.Date

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class SubmissionStateProviderTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var submissionSettings: SubmissionSettings

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(LocalData)

        every { submissionRepository.hasViewedTestResult } returns flow { emit(true) }
        every { submissionRepository.deviceUIStateFlow } returns flow {
            emit(NetworkRequestWrapper.RequestSuccessful<DeviceUIState, Throwable>(DeviceUIState.PAIRED_POSITIVE))
        }
        every { submissionRepository.testResultReceivedDateFlow } returns flow { emit(Date()) }
        every { submissionSettings.registrationToken } returns mockFlowPreference(null)
    }

    private fun createInstance() = SubmissionStateProvider(
        submissionRepository = submissionRepository,
        submissionSettings = submissionSettings
    )

    @Test
    fun `state determination, unregistered test`() = runBlockingTest {
        createInstance().apply {
            state.first() shouldBe NoTest

            verifySequence {
                submissionRepository.deviceUIStateFlow
                submissionRepository.hasViewedTestResult
                submissionRepository.testResultReceivedDateFlow
                submissionSettings.registrationToken
            }
        }
    }
}
