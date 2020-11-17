package de.rki.coronawarnapp.main.home

import android.content.Context
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.ui.main.home.SubmissionCardState
import de.rki.coronawarnapp.ui.main.home.SubmissionCardsStateProvider
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class SubmissionCardsStateProviderTest : BaseTest() {

    @MockK lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(SubmissionRepository)
        mockkObject(LocalData)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = SubmissionCardsStateProvider()

    @Test
    fun `state is combined correctly`() = runBlockingTest {
        every { SubmissionRepository.deviceUIStateFlow } returns flow {
            emit(NetworkRequestWrapper.RequestSuccessful<DeviceUIState, Throwable>(DeviceUIState.PAIRED_POSITIVE))
        }
        every { LocalData.registrationToken() } returns "token"

        createInstance().apply {
            state.first() shouldBe SubmissionCardState(
                deviceUiState = NetworkRequestWrapper.RequestSuccessful(DeviceUIState.PAIRED_POSITIVE),
                isDeviceRegistered = true
            )

            verify {
                SubmissionRepository.deviceUIStateFlow
                LocalData.registrationToken()
            }
        }
    }
}
