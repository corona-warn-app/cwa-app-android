package de.rki.coronawarnapp.submission.task

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
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
import de.rki.coronawarnapp.srs.core.SubmissionReporter
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.submission.task.SubmissionTask.Result.State
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldNotThrow
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

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

    @MockK lateinit var transformedKey: TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
    @MockK lateinit var appConfigData: ConfigData
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector
    @MockK lateinit var checkInsTransformer: CheckInsTransformer
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var submissionReporter: SubmissionReporter

    private val userSymptoms: Symptoms = mockk()

    private val coronaTestsFlow = MutableStateFlow(
        setOf(
            mockk<PersonalCoronaTest>().apply {
                every { isAdvancedConsentGiven } returns true
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
                every { registrationToken } returns "regtoken"
                every { identifier } returns "coronatest-identifier"
                every { type } returns PCR
                every { authCode } returns null
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
        checkInEnd = Instant.EPOCH.plus(9000, ChronoUnit.MILLIS),
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

        coronaTestRepository.apply {
            every { coronaTests } returns coronaTestsFlow
            coEvery { markAsSubmitted("coronatest-identifier") } just Runs
            coEvery { updateAuthCode(any(), any()) } just Runs
        }

        every { tekBatch.keys } returns listOf(tek)
        every { tekHistoryStorage.tekData } returns flowOf(listOf(tekBatch))
        coEvery { tekHistoryStorage.reset() } just Runs

        every {
            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), any())
        } returns listOf(transformedKey)

        every { submissionSettings.symptoms } returns flowOf(userSymptoms)
        every { submissionSettings.lastSubmissionUserActivityUTC } returns flowOf(Instant.EPOCH.plusMillis(1))
        every { submissionSettings.autoSubmissionAttemptsCount } returns flowOf(0)
        every { submissionSettings.autoSubmissionAttemptsLast } returns flowOf(Instant.EPOCH)

        coEvery { submissionSettings.updateAutoSubmissionAttemptsCount(any()) } just Runs
        coEvery { submissionSettings.updateLastSubmissionUserActivityUTC(any()) } just Runs
        coEvery { submissionSettings.updateAutoSubmissionAttemptsLast(any()) } just Runs
        coEvery { submissionSettings.updateSymptoms(any()) } just Runs

        coEvery { appConfigProvider.getAppConfig() } returns appConfigData
        every { appConfigData.supportedCountries } returns listOf("NL")

        coEvery { playbook.submit(any()) } just Runs
        coEvery { playbook.retrieveTan(any(), any()) } returns "tan"
        coEvery { submissionReporter.reportAt(any()) } just Runs

        coEvery { analyticsKeySubmissionCollector.reportSubmitted(any()) } just Runs
        coEvery { analyticsKeySubmissionCollector.reportSubmittedInBackground(any()) } just Runs

        every { testResultAvailableNotificationService.cancelTestResultAvailableNotification() } just Runs

        coEvery { autoSubmission.updateMode(any()) } just Runs

        every { timeStamper.nowUTC } returns Instant.EPOCH.plus(Duration.ofHours(1))

        checkInRepository.apply {
            every { checkInsWithinRetention } returns flowOf(
                listOf(
                    validCheckIn,
                    invalidCheckIn1,
                    invalidCheckIn2,
                    invalidCheckIn3
                )
            )
            coEvery { updatePostSubmissionFlags(any<Long>()) } just Runs
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
        coronaTestRepository = coronaTestRepository,
        submissionReporter = submissionReporter,
    )

    @Test
    fun `submission flow`() = runTest {
        createTask().run(SubmissionTask.Arguments(checkUserActivity = true)) shouldBe SubmissionTask.Result(
            state = State.SUCCESSFUL
        )

        coVerifySequence {
            submissionSettings.lastSubmissionUserActivityUTC
            coronaTestRepository.coronaTests
            submissionSettings.autoSubmissionAttemptsCount
            submissionSettings.autoSubmissionAttemptsLast
            submissionSettings.autoSubmissionAttemptsCount

            submissionSettings.updateAutoSubmissionAttemptsCount(any())
            submissionSettings.updateAutoSubmissionAttemptsLast(any())

            coronaTestRepository.coronaTests
            tekHistoryStorage.tekData
            submissionSettings.symptoms
            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), userSymptoms)
            checkInRepository.checkInsWithinRetention
            checkInsTransformer.transform(any(), any())

            playbook.retrieveTan("regtoken", null)
            coronaTestRepository.updateAuthCode("coronatest-identifier", "tan")

            appConfigProvider.getAppConfig()

            playbook.submit(
                Playbook.SubmissionData(
                    registrationToken = "regtoken",
                    temporaryExposureKeys = listOf(transformedKey),
                    consentToFederation = true,
                    visitedCountries = listOf("NL"),
                    unencryptedCheckIns = emptyList(),
                    encryptedCheckIns = emptyList(),
                    submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST,
                    authCode = "tan"
                )
            )
            tekHistoryStorage.reset()
            submissionSettings.updateSymptoms(null)
            checkInRepository.updatePostSubmissionFlags(validCheckIn.id)
            autoSubmission.updateMode(AutoSubmission.Mode.DISABLED)
            coronaTestRepository.markAsSubmitted(any())
            testResultAvailableNotificationService.cancelTestResultAvailableNotification()
            submissionReporter.reportAt(any())
        }

        coVerify(exactly = 0) {
            checkInRepository.updatePostSubmissionFlags(invalidCheckIn1.id)
            checkInRepository.updatePostSubmissionFlags(invalidCheckIn2.id)
            checkInRepository.updatePostSubmissionFlags(invalidCheckIn3.id)
        }
    }

    @Test
    fun `NO_INFORMATION symptoms are used when the stored symptoms are null`() = runTest {
        val emptySymptoms: Flow<Symptoms?> = flowOf(null)
        every { submissionSettings.symptoms } returns emptySymptoms

        val task = createTask()
        task.run(SubmissionTask.Arguments()) shouldBe SubmissionTask.Result(
            state = State.SUCCESSFUL
        )

        verify {
            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), Symptoms.NO_INFO_GIVEN)
        }
    }

    @Test
    fun `submission data is not deleted if submission fails`() = runTest {
        coEvery { playbook.submit(any()) } throws IOException()

        shouldThrow<IOException> {
            createTask().run(SubmissionTask.Arguments())
        }

        coVerifySequence {
            coronaTestRepository.coronaTests // Consent
            submissionSettings.autoSubmissionAttemptsCount
            submissionSettings.autoSubmissionAttemptsLast
            submissionSettings.autoSubmissionAttemptsCount
            submissionSettings.updateAutoSubmissionAttemptsCount(any())
            submissionSettings.updateAutoSubmissionAttemptsLast(any())
            coronaTestRepository.coronaTests // regToken
            tekHistoryStorage.tekData
            submissionSettings.symptoms

            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), userSymptoms)
            playbook.retrieveTan("regtoken", null)
            coronaTestRepository.updateAuthCode("coronatest-identifier", "tan")
            appConfigProvider.getAppConfig()

            playbook.submit(
                Playbook.SubmissionData(
                    registrationToken = "regtoken",
                    temporaryExposureKeys = listOf(transformedKey),
                    consentToFederation = true,
                    visitedCountries = listOf("NL"),
                    unencryptedCheckIns = emptyList(),
                    encryptedCheckIns = emptyList(),
                    submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST,
                    authCode = "tan"
                )
            )
        }
        coVerify(exactly = 0) {
            tekHistoryStorage.reset()
            checkInRepository.reset()
            submissionSettings.updateSymptoms(any())
            autoSubmission.updateMode(any())
        }
        submissionSettings.symptoms.first() shouldBe userSymptoms
    }

    @Test
    fun `matches playbook pattern if tan retrieval fails`() = runTest {
        coEvery { playbook.retrieveTan("regtoken", null) } throws Exception()
        coEvery { playbook.submitFake() } just Runs

        shouldThrow<Exception> {
            createTask().run(SubmissionTask.Arguments())
        }

        coVerifySequence {
            coronaTestRepository.coronaTests // Consent
            submissionSettings.autoSubmissionAttemptsCount
            submissionSettings.autoSubmissionAttemptsLast
            submissionSettings.autoSubmissionAttemptsCount
            submissionSettings.updateAutoSubmissionAttemptsCount(any())
            submissionSettings.updateAutoSubmissionAttemptsLast(any())
            coronaTestRepository.coronaTests // regToken
            tekHistoryStorage.tekData
            submissionSettings.symptoms
            tekHistoryCalculations.transformToKeyHistoryInExternalFormat(listOf(tek), userSymptoms)
            playbook.retrieveTan("regtoken", null)
            playbook.submitFake()
        }
        coVerify(exactly = 0) {
            tekHistoryStorage.reset()
            checkInRepository.reset()
            submissionSettings.updateSymptoms(any())
            autoSubmission.updateMode(any())
            coronaTestRepository.updateAuthCode(any(), any())
        }
        submissionSettings.symptoms.first() shouldBe userSymptoms
    }

    @Test
    fun `task throws if no registration token is available`() = runTest {
        coronaTestsFlow.value = setOf(
            mockk<PersonalCoronaTest>().apply {
                every { isAdvancedConsentGiven } returns true
                every { isSubmissionAllowed } returns false
                every { isSubmitted } returns false
            }
        )

        val task = createTask()
        shouldNotThrow<IllegalStateException> {
            task.run(SubmissionTask.Arguments()) shouldBe SubmissionTask.Result(state = State.SKIPPED)
        }
    }

    @Test
    fun `DE is used as fallback country`() = runTest {
        every { appConfigData.supportedCountries } returns listOf("DE")

        createTask().run(SubmissionTask.Arguments()) shouldBe SubmissionTask.Result(
            state = State.SUCCESSFUL
        )

        coVerifySequence {
            playbook.retrieveTan("regtoken", null)
            playbook.submit(
                Playbook.SubmissionData(
                    registrationToken = "regtoken",
                    temporaryExposureKeys = listOf(transformedKey),
                    consentToFederation = true,
                    visitedCountries = listOf("DE"),
                    unencryptedCheckIns = emptyList(),
                    encryptedCheckIns = emptyList(),
                    submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST,
                    authCode = "tan"
                )
            )
        }
    }

    @Test
    fun `submission is skipped if user was recently active in submission`() = runTest {
        every { submissionSettings.lastSubmissionUserActivityUTC } returns
            flowOf(Instant.EPOCH.plus(Duration.ofMinutes(33)))
        val task = createTask()
        task.run(SubmissionTask.Arguments(checkUserActivity = true)) shouldBe SubmissionTask.Result(
            state = State.SKIPPED
        )

        coVerify(exactly = 0) { tekHistoryCalculations.transformToKeyHistoryInExternalFormat(any(), any()) }
    }

    @Test
    fun `user activity is only checked if enabled via arguments`() = runTest {
        val task = createTask()

        task.run(SubmissionTask.Arguments(checkUserActivity = false))
        coVerify(exactly = 0) { submissionSettings.lastSubmissionUserActivityUTC }

        task.run(SubmissionTask.Arguments(checkUserActivity = true))
        coVerify { submissionSettings.lastSubmissionUserActivityUTC }
    }

    @Test
    fun `user activity is not checked by default`() {
        SubmissionTask.Arguments().checkUserActivity shouldBe false
    }

    @Test
    fun `negative user activity durations lead to immediate submission`() = runTest {
        submissionSettings.updateLastSubmissionUserActivityUTC(Instant.ofEpochMilli(Long.MAX_VALUE))
        val task = createTask()
        task.run(SubmissionTask.Arguments(checkUserActivity = true)) shouldBe SubmissionTask.Result(
            state = State.SUCCESSFUL
        )
    }

    @Test
    fun `task executed with empty TEKs disables autosubmission too`() = runTest {
        every { tekHistoryStorage.tekData } returns emptyFlow()
        val task = createTask()
        shouldThrow<NoSuchElementException> {
            task.run(SubmissionTask.Arguments())
        }
        coVerify { autoSubmission.updateMode(AutoSubmission.Mode.DISABLED) }
    }

    @Test
    fun `exceeding retry attempts throws error and disables autosubmission`() = runTest {
        every { submissionSettings.autoSubmissionAttemptsCount } returns flowOf(Int.MAX_VALUE)
        val task = createTask()
        shouldThrowMessage("Submission task retry limit exceeded") {
            task.run(SubmissionTask.Arguments())
        }
        coVerify { autoSubmission.updateMode(AutoSubmission.Mode.DISABLED) }
    }

    @Test
    fun `PPA is collected for PCR tests`() = runTest {
        coronaTestsFlow.value = setOf(
            mockk<PersonalCoronaTest>().apply {
                every { type } returns PCR
                every { isAdvancedConsentGiven } returns true
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
                every { registrationToken } returns "regtoken"
                every { identifier } returns "coronatest-identifier"
                every { authCode } returns null
            }
        )

        createTask().run(SubmissionTask.Arguments(checkUserActivity = true))

        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmitted(PCR) }
        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedInBackground(PCR) }

        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmitted(RAPID_ANTIGEN) }
        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedInBackground(RAPID_ANTIGEN) }
    }

    @Test
    fun `PPA is collected for RAT tests`() = runTest {
        coronaTestsFlow.value = setOf(
            mockk<PersonalCoronaTest>().apply {
                every { type } returns RAPID_ANTIGEN
                every { isAdvancedConsentGiven } returns true
                every { isSubmissionAllowed } returns true
                every { isSubmitted } returns false
                every { registrationToken } returns "regtoken"
                every { identifier } returns "coronatest-identifier"
                every { authCode } returns null
            }
        )

        createTask().run(SubmissionTask.Arguments(checkUserActivity = true))

        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmitted(PCR) }
        coVerify(exactly = 0) { analyticsKeySubmissionCollector.reportSubmittedInBackground(PCR) }

        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmitted(RAPID_ANTIGEN) }
        coVerify(exactly = 1) { analyticsKeySubmissionCollector.reportSubmittedInBackground(RAPID_ANTIGEN) }
    }
}
