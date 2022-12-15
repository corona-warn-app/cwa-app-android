package de.rki.coronawarnapp.srs.core.repository

import com.google.gson.JsonObject
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.datadonation.safetynet.AttestationContainer
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetClientWrapper
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsTransformer
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.srs.core.AndroidIdProvider
import de.rki.coronawarnapp.srs.core.SubmissionReporter
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.model.SrsOtp
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.core.playbook.SrsPlaybook
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.submission.task.ExposureKeyHistoryCalculations
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.util.UUID

internal class SrsSubmissionRepositoryTest : BaseTest() {

    @MockK lateinit var playbook: SrsPlaybook
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var tekCalculations: ExposureKeyHistoryCalculations
    @MockK lateinit var tekStorage: TEKHistoryStorage
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var checkInsRepo: CheckInRepository
    @MockK lateinit var checkInsTransformer: CheckInsTransformer
    @MockK lateinit var deviceAttestation: DeviceAttestation
    @MockK lateinit var srsSubmissionSettings: SrsSubmissionSettings
    @MockK lateinit var androidIdProvider: AndroidIdProvider
    @MockK lateinit var submissionReporter: SubmissionReporter
    @MockK lateinit var srsDevSettings: SrsDevSettings

    @MockK lateinit var attestationContainer: AttestationContainer
    @MockK lateinit var configData: ConfigData

    private val srsOtp = SrsOtp(
        uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"),
        expiresAt = Instant.parse("2023-11-07T12:10:10Z")
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { attestationContainer.requirePass(any()) } just Runs
        every { attestationContainer.report } returns SafetyNetClientWrapper.Report(
            jwsResult = "",
            header = JsonObject(),
            body = JsonObject(),
            signature = byteArrayOf()
        )
        every { attestationContainer.ourSalt } returns byteArrayOf()

        every { configData.selfReportSubmission.ppac } returns SafetyNetRequirementsContainer()
        every { configData.supportedCountries } returns listOf()

        every { timeStamper.nowUTC } returns Instant.parse("2022-11-07T12:10:10Z")
        every { tekStorage.tekData } returns flowOf(listOf())
        every { tekCalculations.transformToKeyHistoryInExternalFormat(any(), any()) } returns emptyList()
        every { checkInsRepo.completedCheckIns } returns flowOf(emptyList())
        every { androidIdProvider.getAndroidId() } returns ByteString.EMPTY

        coEvery { checkInsTransformer.transform(any(), any()) } returns CheckInsReport(
            unencryptedCheckIns = emptyList(),
            encryptedCheckIns = emptyList()
        )
        coEvery { checkInsRepo.updatePostSubmissionFlags(any<List<CheckIn>>()) } just Runs
        coEvery { tekStorage.reset() } just Runs
        coEvery { deviceAttestation.attest(any()) } returns attestationContainer
        coEvery { srsSubmissionSettings.getOtp() } returns null
        coEvery { srsSubmissionSettings.resetOtp() } just Runs
        coEvery { srsSubmissionSettings.setOtp(any()) } just Runs
        coEvery { srsSubmissionSettings.setMostRecentSubmissionTime(any()) } just Runs
        coEvery { appConfigProvider.getAppConfig() } returns configData
        coEvery { playbook.authorize(any()) } returns Instant.parse("2023-11-07T12:10:10Z")
        coEvery { playbook.submit(any()) } just Runs
        coEvery { playbook.fakeAuthorize(any()) } just Runs
        coEvery { srsDevSettings.checkLocalPrerequisites() } returns true
        coEvery { submissionReporter.reportAt(any()) } just Runs
    }

    @Test
    fun `submit sequence - no prev otp`() = runTest {
        instance().submit(SrsSubmissionType.SRS_SELF_TEST)
        coVerifySequence {
            appConfigProvider.getAppConfig()
            timeStamper.nowUTC
            srsSubmissionSettings.getOtp()
            androidIdProvider.getAndroidId()
            playbook.authorize(any())
            srsSubmissionSettings.setOtp(any())
            tekStorage.tekData
            tekCalculations.transformToKeyHistoryInExternalFormat(any(), any())
            checkInsRepo.completedCheckIns
            checkInsTransformer.transform(any(), any())
            playbook.submit(any())
            tekStorage.reset()
            checkInsRepo.updatePostSubmissionFlags(any<List<CheckIn>>())
            timeStamper.nowUTC
            submissionReporter.reportAt(any())
            srsSubmissionSettings.resetOtp()
        }

        coVerify(exactly = 0) {
            playbook.fakeAuthorize(any())
        }
    }

    @Test
    fun `submit sequence - no valid otp`() = runTest {
        coEvery { srsSubmissionSettings.getOtp() } returns SrsOtp(
            uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"),
            expiresAt = Instant.parse("2021-11-07T12:10:10Z")
        )
        instance().submit(SrsSubmissionType.SRS_SELF_TEST)
        coVerifySequence {
            appConfigProvider.getAppConfig()
            timeStamper.nowUTC
            srsSubmissionSettings.getOtp()
            androidIdProvider.getAndroidId()
            playbook.authorize(any())
            srsSubmissionSettings.setOtp(any())
            tekStorage.tekData
            tekCalculations.transformToKeyHistoryInExternalFormat(any(), any())
            checkInsRepo.completedCheckIns
            checkInsTransformer.transform(any(), any())
            playbook.submit(any())
            tekStorage.reset()
            checkInsRepo.updatePostSubmissionFlags(any<List<CheckIn>>())
            timeStamper.nowUTC
            submissionReporter.reportAt(any())
            srsSubmissionSettings.resetOtp()
        }

        coVerify(exactly = 0) {
            playbook.fakeAuthorize(any())
        }
    }

    @Test
    fun `submit sequence - valid otp`() = runTest {
        coEvery { srsSubmissionSettings.getOtp() } returns srsOtp
        instance().submit(SrsSubmissionType.SRS_SELF_TEST)
        coVerifySequence {
            appConfigProvider.getAppConfig()
            timeStamper.nowUTC
            srsSubmissionSettings.getOtp()
            playbook.fakeAuthorize(any())
            tekStorage.tekData
            tekCalculations.transformToKeyHistoryInExternalFormat(any(), any())
            checkInsRepo.completedCheckIns
            checkInsTransformer.transform(any(), any())
            playbook.submit(any())
            tekStorage.reset()
            checkInsRepo.updatePostSubmissionFlags(any<List<CheckIn>>())
            timeStamper.nowUTC
            submissionReporter.reportAt(any())
            srsSubmissionSettings.resetOtp()
        }

        coVerify(exactly = 0) {
            playbook.authorize(any())
        }
    }

    @Test
    fun `currentOtp is still valid  - current time equals expiry time`() = runTest {
        val srsOtp = SrsOtp(
            uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"),
            expiresAt = Instant.parse("2022-11-07T12:10:10Z")
        )
        coEvery { srsSubmissionSettings.getOtp() } returns srsOtp
        instance().currentOtp(Instant.parse("2022-11-07T12:10:10Z")) shouldBe srsOtp
    }

    @Test
    fun `currentOtp is still valid  - current time is less than expiry time`() = runTest {
        val srsOtp = SrsOtp(
            uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"),
            expiresAt = Instant.parse("2023-11-07T12:10:10Z")
        )
        coEvery { srsSubmissionSettings.getOtp() } returns srsOtp
        instance().currentOtp(Instant.parse("2022-11-07T12:10:10Z")) shouldBe srsOtp
    }

    @Test
    fun `currentOtp is not valid  - current time is greater than expiry time`() = runTest {
        val srsOtp = SrsOtp(
            uuid = UUID.fromString("73a373fd-3a7b-49b9-b71c-2ae7a2824760"),
            expiresAt = Instant.parse("2021-11-07T12:10:10Z")
        )
        coEvery { srsSubmissionSettings.getOtp() } returns srsOtp
        instance().currentOtp(Instant.parse("2022-11-07T12:10:10Z")).apply {
            this shouldNotBe srsOtp
            expiresAt shouldBe Instant.MIN
        }
    }

    @Test
    fun `No current Otp`() = runTest {
        coEvery { srsSubmissionSettings.getOtp() } returns null
        instance().currentOtp(Instant.parse("2022-11-07T12:10:10Z")).apply {
            this shouldNotBe null
            expiresAt shouldBe Instant.MIN
        }
    }

    @Test
    fun `attest pass`() = runTest {
        instance().attest(configData, srsOtp, ByteString.EMPTY) shouldBe attestationContainer
    }

    @Test
    fun `attest - error PLAY_SERVICES_VERSION_MISMATCH`() = runTest {
        coEvery { deviceAttestation.attest(any()) } throws SafetyNetException(
            SafetyNetException.Type.PLAY_SERVICES_VERSION_MISMATCH
        )
        shouldThrow<SrsSubmissionException> {
            instance().attest(configData, srsOtp, ByteString.EMPTY)
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.PLAY_SERVICES_VERSION_MISMATCH
    }

    @Test
    fun `attest - error EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED`() = runTest {
        coEvery { deviceAttestation.attest(any()) } throws SafetyNetException(
            SafetyNetException.Type.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED
        )
        shouldThrow<SrsSubmissionException> {
            instance().attest(configData, srsOtp, ByteString.EMPTY)
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED
    }

    @Test
    fun `attest - error EVALUATION_TYPE_BASIC_REQUIRED`() = runTest {
        coEvery { deviceAttestation.attest(any()) } throws SafetyNetException(
            SafetyNetException.Type.EVALUATION_TYPE_BASIC_REQUIRED
        )
        shouldThrow<SrsSubmissionException> {
            instance().attest(configData, srsOtp, ByteString.EMPTY)
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.EVALUATION_TYPE_BASIC_REQUIRED
    }

    @Test
    fun `attest - error APK_PACKAGE_NAME_MISMATCH`() = runTest {
        coEvery { deviceAttestation.attest(any()) } throws SafetyNetException(
            SafetyNetException.Type.APK_PACKAGE_NAME_MISMATCH
        )
        shouldThrow<SrsSubmissionException> {
            instance().attest(configData, srsOtp, ByteString.EMPTY)
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.APK_PACKAGE_NAME_MISMATCH
    }

    @Test
    fun `attest - error ATTESTATION_FAILED`() = runTest {
        coEvery { deviceAttestation.attest(any()) } throws SafetyNetException(
            SafetyNetException.Type.ATTESTATION_FAILED
        )
        shouldThrow<SrsSubmissionException> {
            instance().attest(configData, srsOtp, ByteString.EMPTY)
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.ATTESTATION_FAILED
    }

    @Test
    fun `attest - error INTERNAL_ERROR`() = runTest {
        coEvery { deviceAttestation.attest(any()) } throws SafetyNetException(
            SafetyNetException.Type.INTERNAL_ERROR
        )
        shouldThrow<SrsSubmissionException> {
            instance().attest(configData, srsOtp, ByteString.EMPTY)
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.ATTESTATION_FAILED
    }

    @Test
    fun `attest - error ATTESTATION_REQUEST_FAILED`() = runTest {
        coEvery { deviceAttestation.attest(any()) } throws SafetyNetException(
            SafetyNetException.Type.ATTESTATION_REQUEST_FAILED
        )
        shouldThrow<SrsSubmissionException> {
            instance().attest(configData, srsOtp, ByteString.EMPTY)
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.ATTESTATION_REQUEST_FAILED
    }

    @Test
    fun `attest - other error`() = runTest {
        coEvery { deviceAttestation.attest(any()) } throws Exception("Surprise!")
        shouldThrow<Exception> {
            instance().attest(configData, srsOtp, ByteString.EMPTY)
        }.message shouldBe "Surprise!"
    }

    private fun instance() = SrsSubmissionRepository(
        playbook = playbook,
        appConfigProvider = appConfigProvider,
        tekCalculations = tekCalculations,
        tekStorage = tekStorage,
        checkInsRepo = checkInsRepo,
        checkInsTransformer = checkInsTransformer,
        deviceAttestation = deviceAttestation,
        srsSubmissionSettings = srsSubmissionSettings,
        androidIdProvider = androidIdProvider,
        timeStamper = timeStamper,
        submissionReporter = submissionReporter,
        srsDevSettings = srsDevSettings,
    )
}
