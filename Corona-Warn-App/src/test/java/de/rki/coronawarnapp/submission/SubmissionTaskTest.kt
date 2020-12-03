package de.rki.coronawarnapp.submission

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference
import java.io.IOException

class SubmissionTaskTest : BaseTest() {

    @MockK lateinit var playbook: Playbook
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var tekHistoryCalculations: ExposureKeyHistoryCalculations
    @MockK lateinit var tekHistoryStorage: TEKHistoryStorage
    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var testResultNotificationService: TestResultNotificationService

    @MockK lateinit var tekBatch: TEKHistoryStorage.TEKBatch
    @MockK lateinit var tek: TemporaryExposureKey
    @MockK lateinit var userSymptoms: Symptoms
    @MockK lateinit var transformedKey: TemporaryExposureKeyExportOuterClass.TemporaryExposureKey

    @MockK lateinit var appConfigData: ConfigData

    private lateinit var mockSymptomsPreference: FlowPreference<Symptoms?>

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)
        every { LocalData.registrationToken() } returns "regtoken"
        every { LocalData.numberOfSuccessfulSubmissions(any()) } just Runs

        mockkObject(BackgroundWorkScheduler)
        every { BackgroundWorkScheduler.stopWorkScheduler() } just Runs

        every { tekBatch.keys } returns listOf(tek)
        every { tekHistoryStorage.tekData } returns flowOf(listOf(tekBatch))
        coEvery { tekHistoryStorage.clear() } just Runs

        every {
            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), userSymptoms)
        } returns listOf(transformedKey)

        mockSymptomsPreference = mockFlowPreference(userSymptoms)
        every { submissionSettings.symptoms } returns mockSymptomsPreference

        coEvery { appConfigProvider.getAppConfig() } returns appConfigData
        every { appConfigData.supportedCountries } returns listOf("NL")

        coEvery { playbook.submit(any()) } just Runs

        every { testResultNotificationService.cancelPositiveTestResultNotification() } just Runs
    }

    private fun createTask() = SubmissionTask(
        playbook = playbook,
        appConfigProvider = appConfigProvider,
        tekHistoryCalculations = tekHistoryCalculations,
        tekHistoryStorage = tekHistoryStorage,
        submissionSettings = submissionSettings,
        testResultNotificationService
    )

    @Test
    fun `submission flow`() = runBlockingTest {
        val task = createTask()
        task.run(object : Task.Arguments {})

        coVerifySequence {
            LocalData.registrationToken()
            tekHistoryStorage.tekData
            mockSymptomsPreference.value

            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), userSymptoms)

            appConfigProvider.getAppConfig()
            playbook.submit(
                Playbook.SubmissionData(
                    "regtoken",
                    listOf(transformedKey),
                    true,
                    listOf("NL")
                )
            )

            tekHistoryStorage.clear()
            mockSymptomsPreference.update(any())

            BackgroundWorkScheduler.stopWorkScheduler()
            LocalData.numberOfSuccessfulSubmissions(1)

            testResultNotificationService.cancelPositiveTestResultNotification()
        }

        submissionSettings.symptoms.value shouldBe null
    }

    @Test
    fun `submission data is not deleted if submission fails`() = runBlockingTest {
        coEvery { playbook.submit(any()) } throws IOException()

        shouldThrow<IOException> {
            createTask().run(object : Task.Arguments {})
        }

        coVerifySequence {
            LocalData.registrationToken()
            tekHistoryStorage.tekData
            mockSymptomsPreference.value

            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), userSymptoms)

            appConfigProvider.getAppConfig()
            playbook.submit(
                Playbook.SubmissionData(
                    "regtoken",
                    listOf(transformedKey),
                    true,
                    listOf("NL")
                )
            )
        }
        coVerify(exactly = 0) {
            tekHistoryStorage.clear()
            mockSymptomsPreference.update(any())
            testResultNotificationService.cancelPositiveTestResultNotification()
        }
        submissionSettings.symptoms.value shouldBe userSymptoms
    }

    @Test
    fun `task throws if no registration token is available`() = runBlockingTest {
        every { LocalData.registrationToken() } returns null

        val task = createTask()
        shouldThrow<NoRegistrationTokenSetException> {
            task.run(object : Task.Arguments {})
        }
    }

    @Test
    fun `DE is used as fallback country`() = runBlockingTest {
        every { appConfigData.supportedCountries } returns listOf("DE")

        createTask().run(object : Task.Arguments {})

        coVerifySequence {
            playbook.submit(
                Playbook.SubmissionData(
                    "regtoken",
                    listOf(transformedKey),
                    true,
                    listOf("DE")
                )
            )
        }
    }

    @Test
    fun `NO_INFORMATION symptoms are used when the stored symptoms are null`() {
        // TODO
    }
}
