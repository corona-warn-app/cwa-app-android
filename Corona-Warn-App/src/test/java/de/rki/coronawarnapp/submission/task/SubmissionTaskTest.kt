package de.rki.coronawarnapp.submission.task

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.notification.TestResultAvailableNotificationService
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.joda.time.Instant
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
    @MockK lateinit var shareTestResultNotificationService: ShareTestResultNotificationService
    @MockK lateinit var testResultAvailableNotificationService: TestResultAvailableNotificationService
    @MockK lateinit var autoSubmission: AutoSubmission

    @MockK lateinit var tekBatch: TEKHistoryStorage.TEKBatch
    @MockK lateinit var tek: TemporaryExposureKey
    @MockK lateinit var userSymptoms: Symptoms
    @MockK lateinit var transformedKey: TemporaryExposureKeyExportOuterClass.TemporaryExposureKey

    @MockK lateinit var appConfigData: ConfigData

    @MockK lateinit var timeStamper: TimeStamper

    private lateinit var settingSymptomsPreference: FlowPreference<Symptoms?>

    private val settingHasGivenConsent: FlowPreference<Boolean> = mockFlowPreference(true)
    private val settingAutoSubmissionAttemptsCount: FlowPreference<Int> = mockFlowPreference(0)
    private val settingAutoSubmissionAttemptsLast: FlowPreference<Instant> = mockFlowPreference(Instant.EPOCH)

    private val settingLastUserActivityUTC: FlowPreference<Instant> = mockFlowPreference(Instant.EPOCH.plus(1))

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
            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), any())
        } returns listOf(transformedKey)

        settingSymptomsPreference = mockFlowPreference(userSymptoms)
        every { submissionSettings.symptoms } returns settingSymptomsPreference
        every { submissionSettings.hasGivenConsent } returns settingHasGivenConsent
        every { submissionSettings.lastSubmissionUserActivityUTC } returns settingLastUserActivityUTC
        every { submissionSettings.autoSubmissionAttemptsCount } returns settingAutoSubmissionAttemptsCount
        every { submissionSettings.autoSubmissionAttemptsLast } returns settingAutoSubmissionAttemptsLast

        coEvery { appConfigProvider.getAppConfig() } returns appConfigData
        every { appConfigData.supportedCountries } returns listOf("NL")

        coEvery { playbook.submit(any()) } just Runs

        every { shareTestResultNotificationService.cancelSharePositiveTestResultNotification() } just Runs
        every { testResultAvailableNotificationService.cancelTestResultAvailableNotification() } just Runs

        every { autoSubmission.updateMode(any()) } just Runs

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(1))
    }

    private fun createTask() = SubmissionTask(
        playbook = playbook,
        appConfigProvider = appConfigProvider,
        tekHistoryCalculations = tekHistoryCalculations,
        tekHistoryStorage = tekHistoryStorage,
        submissionSettings = submissionSettings,
        shareTestResultNotificationService = shareTestResultNotificationService,
        timeStamper = timeStamper,
        autoSubmission = autoSubmission,
        testResultAvailableNotificationService = testResultAvailableNotificationService
    )

    @Test
    fun `submission flow`() = runBlockingTest {
        val task = createTask()
        task.run(SubmissionTask.Arguments(checkUserActivity = true)) shouldBe SubmissionTask.Result(
            state = SubmissionTask.Result.State.SUCCESSFUL
        )

        coVerifySequence {
            settingLastUserActivityUTC.value
            settingHasGivenConsent.value

            LocalData.registrationToken()
            tekHistoryStorage.tekData
            settingSymptomsPreference.value

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
            settingSymptomsPreference.update(match { it.invoke(mockk()) == null })

            autoSubmission.updateMode(AutoSubmission.Mode.DISABLED)

            BackgroundWorkScheduler.stopWorkScheduler()
            LocalData.numberOfSuccessfulSubmissions(1)

            shareTestResultNotificationService.cancelSharePositiveTestResultNotification()
            testResultAvailableNotificationService.cancelTestResultAvailableNotification()
        }
    }

    @Test
    fun `NO_INFORMATION symptoms are used when the stored symptoms are null`() = runBlockingTest {
        val emptySymptoms: FlowPreference<Symptoms?> = mockFlowPreference(null)
        every { submissionSettings.symptoms } returns emptySymptoms

        val task = createTask()
        task.run(SubmissionTask.Arguments()) shouldBe SubmissionTask.Result(
            state = SubmissionTask.Result.State.SUCCESSFUL
        )

        verify {
            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), Symptoms.NO_INFO_GIVEN)
        }
    }

    @Test
    fun `submission data is not deleted if submission fails`() = runBlockingTest {
        coEvery { playbook.submit(any()) } throws IOException()

        shouldThrow<IOException> {
            createTask().run(SubmissionTask.Arguments())
        }

        coVerifySequence {
            settingHasGivenConsent.value

            LocalData.registrationToken()
            tekHistoryStorage.tekData
            settingSymptomsPreference.value

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
            settingSymptomsPreference.update(any())
            shareTestResultNotificationService.cancelSharePositiveTestResultNotification()
            autoSubmission.updateMode(any())
        }
        submissionSettings.symptoms.value shouldBe userSymptoms
    }

    @Test
    fun `task throws if no registration token is available`() = runBlockingTest {
        every { LocalData.registrationToken() } returns null

        val task = createTask()
        shouldThrow<NoRegistrationTokenSetException> {
            task.run(SubmissionTask.Arguments())
        }
    }

    @Test
    fun `DE is used as fallback country`() = runBlockingTest {
        every { appConfigData.supportedCountries } returns listOf("DE")

        createTask().run(SubmissionTask.Arguments()) shouldBe SubmissionTask.Result(
            state = SubmissionTask.Result.State.SUCCESSFUL
        )

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
    fun `submission is skipped if user was recently active in submission`() = runBlockingTest {
        settingLastUserActivityUTC.update { Instant.EPOCH.plus(Duration.standardHours(1)) }
        val task = createTask()
        task.run(SubmissionTask.Arguments(checkUserActivity = true)) shouldBe SubmissionTask.Result(
            state = SubmissionTask.Result.State.SKIPPED
        )

        coVerify(exactly = 0) { tekHistoryCalculations.transformToKeyHistoryInExternalFormat(any(), any()) }
    }

    @Test
    fun `user activity is only checked if enabled via arguments`() = runBlockingTest {
        val task = createTask()

        task.run(SubmissionTask.Arguments(checkUserActivity = false))
        verify(exactly = 0) { settingLastUserActivityUTC.value }

        task.run(SubmissionTask.Arguments(checkUserActivity = true))
        verify { settingLastUserActivityUTC.value }
    }

    @Test
    fun `user activity is not checked by default`() {
        SubmissionTask.Arguments().checkUserActivity shouldBe false
    }

    @Test
    fun `negative user activity durations lead to immediate submission`() = runBlockingTest {
        settingLastUserActivityUTC.update { Instant.ofEpochMilli(Long.MAX_VALUE) }
        val task = createTask()
        task.run(SubmissionTask.Arguments(checkUserActivity = true)) shouldBe SubmissionTask.Result(
            state = SubmissionTask.Result.State.SUCCESSFUL
        )
    }

    @Test
    fun `task executed with empty TEKs disables autosubmission too`() = runBlockingTest {
        every { tekHistoryStorage.tekData } returns emptyFlow()
        val task = createTask()
        shouldThrow<NoSuchElementException> {
            task.run(SubmissionTask.Arguments())
        }
        verify { autoSubmission.updateMode(AutoSubmission.Mode.DISABLED) }
    }

    @Test
    fun `exceeding retry attempts throws error and disables autosubmission`() = runBlockingTest {
        settingAutoSubmissionAttemptsCount.update { Int.MAX_VALUE }
        val task = createTask()
        shouldThrowMessage("Submission task retry limit exceeded") {
            task.run(SubmissionTask.Arguments())
        }
        verify { autoSubmission.updateMode(AutoSubmission.Mode.DISABLED) }
    }
}
