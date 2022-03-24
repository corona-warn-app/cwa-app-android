package de.rki.coronawarnapp.submission.task

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.coronatest.PersonalTestRepository
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsTransformer
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload.SubmissionType
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.preferences.FlowPreference
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
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
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
    @MockK lateinit var testResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @MockK lateinit var autoSubmission: AutoSubmission

    @MockK lateinit var tekBatch: TEKHistoryStorage.TEKBatch
    @MockK lateinit var tek: TemporaryExposureKey
    @MockK lateinit var userSymptoms: Symptoms
    @MockK lateinit var transformedKey: TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
    @MockK lateinit var appConfigData: ConfigData
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var checkInsTransformer: CheckInsTransformer
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var personalTestRepository: PersonalTestRepository

    private lateinit var settingSymptomsPreference: FlowPreference<Symptoms?>

    private val settingAutoSubmissionAttemptsCount: FlowPreference<Int> = mockFlowPreference(0)
    private val settingAutoSubmissionAttemptsLast: FlowPreference<Instant> = mockFlowPreference(Instant.EPOCH)

    private val settingLastUserActivityUTC: FlowPreference<Instant> = mockFlowPreference(Instant.EPOCH.plus(1))

    private val coronaTestsFlow = MutableStateFlow(
        setOf(
            mockk<PersonalCoronaTest>().apply {
                every { isAdvancedConsentGiven } returns true
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
                every { registrationToken } returns "regtoken"
                every { identifier } returns "coronatest-identifier"
                every { type } returns PCR
            }
        )
    )

    private val validCheckIn = CheckIn(
        id = 1L,
        traceLocationId = mockk(),
        version = 1,
        type = 2,
        description = "brothers birthday",
        address = "Malibu",
        traceLocationStart = Instant.EPOCH,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = mockk(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.EPOCH,
        checkInEnd = Instant.EPOCH.plus(9000),
        completed = true,
        createJournalEntry = false,
        isSubmitted = false,
        hasSubmissionConsent = true
    )

    private val invalidCheckIn1 = validCheckIn.copy(id = 2L, completed = false)
    private val invalidCheckIn2 = validCheckIn.copy(id = 3L, isSubmitted = true)
    private val invalidCheckIn3 = validCheckIn.copy(id = 4L, hasSubmissionConsent = false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        personalTestRepository.apply {
            every { coronaTests } returns coronaTestsFlow
            coEvery { markAsSubmitted("coronatest-identifier") } just Runs
        }

        every { tekBatch.keys } returns listOf(tek)
        every { tekHistoryStorage.tekData } returns flowOf(listOf(tekBatch))
        coEvery { tekHistoryStorage.clear() } just Runs

        every {
            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), any())
        } returns listOf(transformedKey)

        settingSymptomsPreference = mockFlowPreference(userSymptoms)
        every { submissionSettings.symptoms } returns settingSymptomsPreference
        every { submissionSettings.lastSubmissionUserActivityUTC } returns settingLastUserActivityUTC
        every { submissionSettings.autoSubmissionAttemptsCount } returns settingAutoSubmissionAttemptsCount
        every { submissionSettings.autoSubmissionAttemptsLast } returns settingAutoSubmissionAttemptsLast

        coEvery { appConfigProvider.getAppConfig() } returns appConfigData
        every { appConfigData.supportedCountries } returns listOf("NL")

        coEvery { playbook.submit(any()) } just Runs

        every { analyticsKeySubmissionCollector.reportSubmitted(any()) } just Runs
        every { analyticsKeySubmissionCollector.reportSubmittedInBackground(any()) } just Runs

        every { testResultAvailableNotificationService.cancelTestResultAvailableNotification() } just Runs

        every { autoSubmission.updateMode(any()) } just Runs

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.standardHours(1))

        checkInRepository.apply {
            every { checkInsWithinRetention } returns flowOf(
                listOf(
                    validCheckIn,
                    invalidCheckIn1,
                    invalidCheckIn2,
                    invalidCheckIn3
                )
            )
            coEvery { updatePostSubmissionFlags(any()) } just Runs
        }

        coEvery { checkInsTransformer.transform(any(), any()) } returns CheckInsReport(emptyList(), emptyList())
    }

    private fun createTask() = SubmissionTask(
        playbook = playbook,
        appConfigProvider = appConfigProvider,
        tekHistoryCalculations = tekHistoryCalculations,
        tekHistoryStorage = tekHistoryStorage,
        submissionSettings = submissionSettings,
        timeStamper = timeStamper,
        autoSubmission = autoSubmission,
        testResultAvailableNotificationService = testResultAvailableNotificationService,
        analyticsKeySubmissionCollector = analyticsKeySubmissionCollector,
        checkInsRepository = checkInRepository,
        checkInsTransformer = checkInsTransformer,
        coronaTestRepository = personalTestRepository,
    )

    @Test
    fun `submission flow`() = runBlockingTest {
        val task = createTask()
        task.run(SubmissionTask.Arguments(checkUserActivity = true)) shouldBe SubmissionTask.Result(
            state = SubmissionTask.Result.State.SUCCESSFUL
        )

        coVerifySequence {
            submissionSettings.lastSubmissionUserActivityUTC
            settingLastUserActivityUTC.value
            personalTestRepository.coronaTests

            submissionSettings.autoSubmissionAttemptsCount
            submissionSettings.autoSubmissionAttemptsLast
            submissionSettings.autoSubmissionAttemptsCount
            submissionSettings.autoSubmissionAttemptsLast

            personalTestRepository.coronaTests
            tekHistoryStorage.tekData
            submissionSettings.symptoms
            settingSymptomsPreference.value

            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), userSymptoms)
            checkInRepository.checkInsWithinRetention
            checkInsTransformer.transform(any(), any())

            appConfigProvider.getAppConfig()
            playbook.submit(
                Playbook.SubmissionData(
                    registrationToken = "regtoken",
                    temporaryExposureKeys = listOf(transformedKey),
                    consentToFederation = true,
                    visitedCountries = listOf("NL"),
                    unencryptedCheckIns = emptyList(),
                    encryptedCheckIns = emptyList(),
                    submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
                )
            )

            tekHistoryStorage.clear()
            submissionSettings.symptoms
            settingSymptomsPreference.update(match { it.invoke(mockk()) == null })

            checkInRepository.updatePostSubmissionFlags(validCheckIn.id)

            autoSubmission.updateMode(AutoSubmission.Mode.DISABLED)

            personalTestRepository.markAsSubmitted(any())

            testResultAvailableNotificationService.cancelTestResultAvailableNotification()
        }

        coVerify(exactly = 0) {
            checkInRepository.updatePostSubmissionFlags(invalidCheckIn1.id)
            checkInRepository.updatePostSubmissionFlags(invalidCheckIn2.id)
            checkInRepository.updatePostSubmissionFlags(invalidCheckIn3.id)
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
            personalTestRepository.coronaTests // Consent
            personalTestRepository.coronaTests // regToken
            tekHistoryStorage.tekData
            settingSymptomsPreference.value

            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), userSymptoms)

            appConfigProvider.getAppConfig()
            playbook.submit(
                Playbook.SubmissionData(
                    registrationToken = "regtoken",
                    temporaryExposureKeys = listOf(transformedKey),
                    consentToFederation = true,
                    visitedCountries = listOf("NL"),
                    unencryptedCheckIns = emptyList(),
                    encryptedCheckIns = emptyList(),
                    submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
                )
            )
        }
        coVerify(exactly = 0) {
            tekHistoryStorage.clear()
            checkInRepository.clear()
            settingSymptomsPreference.update(any())
            autoSubmission.updateMode(any())
        }
        submissionSettings.symptoms.value shouldBe userSymptoms
    }

    @Test
    fun `task throws if no registration token is available`() = runBlockingTest {
        coronaTestsFlow.value = setOf(
            mockk<PersonalCoronaTest>().apply {
                every { isAdvancedConsentGiven } returns true
                every { isSubmissionAllowed } returns false
                every { isSubmitted } returns false
            }
        )

        val task = createTask()
        shouldThrow<IllegalStateException> {
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
                    registrationToken = "regtoken",
                    temporaryExposureKeys = listOf(transformedKey),
                    consentToFederation = true,
                    visitedCountries = listOf("DE"),
                    unencryptedCheckIns = emptyList(),
                    encryptedCheckIns = emptyList(),
                    submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
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

    @Test
    fun `PPA is collected for PCR tests`() = runBlockingTest {
        coronaTestsFlow.value = setOf(
            mockk<PersonalCoronaTest>().apply {
                every { type } returns PCR
                every { isAdvancedConsentGiven } returns true
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
                every { registrationToken } returns "regtoken"
                every { identifier } returns "coronatest-identifier"
            }
        )

        createTask().run(SubmissionTask.Arguments(checkUserActivity = true))

        verify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmitted(PCR) }
        verify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedInBackground(PCR) }

        verify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmitted(RAPID_ANTIGEN) }
        verify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedInBackground(RAPID_ANTIGEN) }
    }

    @Test
    fun `PPA is collected for RAT tests`() = runBlockingTest {
        coronaTestsFlow.value = setOf(
            mockk<PersonalCoronaTest>().apply {
                every { type } returns RAPID_ANTIGEN
                every { isAdvancedConsentGiven } returns true
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
                every { registrationToken } returns "regtoken"
                every { identifier } returns "coronatest-identifier"
            }
        )

        createTask().run(SubmissionTask.Arguments(checkUserActivity = true))

        verify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmitted(PCR) }
        verify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedInBackground(PCR) }

        verify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmitted(RAPID_ANTIGEN) }
        verify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedInBackground(RAPID_ANTIGEN) }
    }
}
