package de.rki.coronawarnapp.srs.core.repository

import androidx.annotation.VisibleForTesting
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.getSupportedCountries
import de.rki.coronawarnapp.bugreporting.censors.submission.OtpCensor
import de.rki.coronawarnapp.datadonation.safetynet.AttestationContainer
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsTransformer
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtpRequestAndroid.SRSOneTimePasswordRequestAndroid
import de.rki.coronawarnapp.srs.core.AndroidIdProvider
import de.rki.coronawarnapp.srs.core.SubmissionReporter
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationFakeRequest
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationRequest
import de.rki.coronawarnapp.srs.core.model.SrsDeviceAttestationRequest
import de.rki.coronawarnapp.srs.core.model.SrsOtp
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionPayload
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.core.playbook.SrsPlaybook
import de.rki.coronawarnapp.srs.core.server.errorArgs
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.submission.task.ExposureKeyHistoryCalculations
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

@Suppress("LongParameterList")
class SrsSubmissionRepository @Inject constructor(
    private val playbook: SrsPlaybook,
    private val appConfigProvider: AppConfigProvider,
    private val tekCalculations: ExposureKeyHistoryCalculations,
    private val tekStorage: TEKHistoryStorage,
    private val timeStamper: TimeStamper,
    private val checkInsRepo: CheckInRepository,
    private val checkInsTransformer: CheckInsTransformer,
    private val deviceAttestation: DeviceAttestation,
    private val srsSubmissionSettings: SrsSubmissionSettings,
    private val srsDevSettings: SrsDevSettings,
    private val androidIdProvider: AndroidIdProvider,
    private val submissionReporter: SubmissionReporter,
) {
    suspend fun submit(
        type: SrsSubmissionType,
        symptoms: Symptoms = Symptoms.NO_INFO_GIVEN
    ) {
        Timber.tag(TAG).d("submit(type=%s)", type)
        val appConfig = appConfigProvider.getAppConfig()
        val nowUtc = timeStamper.nowUTC
        var srsOtp = currentOtp(nowUtc).also { OtpCensor.otp = it }
        val androidId = androidIdProvider.getAndroidId()
        val attestResult = attest(
            appConfig = appConfig,
            srsOtp = srsOtp,
            androidId = androidId,
            checkDeviceTime = srsDevSettings.checkLocalPrerequisites()
        )

        if (srsOtp.isValid(nowUtc)) {
            Timber.d("Otp is still valid -> fakePlaybookAuthorization")
            playbook.fakeAuthorize(
                SrsAuthorizationFakeRequest(
                    safetyNetJws = attestResult.report.jwsResult,
                    salt = attestResult.ourSalt.toByteString().base64(),
                )
            )
        } else {
            Timber.d("Authorize new srsOtp=%s", srsOtp)
            val expiresAt = playbook.authorize(
                SrsAuthorizationRequest(
                    srsOtp = srsOtp,
                    safetyNetJws = attestResult.report.jwsResult,
                    salt = attestResult.ourSalt.toByteString().base64(),
                    androidId = androidId
                )
            )
            srsOtp = srsOtp.copy(expiresAt = expiresAt)
            srsSubmissionSettings.setOtp(srsOtp)
        }

        val keys: List<TemporaryExposureKey> = runCatching {
            tekStorage.tekData.first().flatMap { it.keys }
        }.onFailure {
            Timber.w(it, "No temporary exposure keys")
        }.getOrDefault(emptyList())

        val transformedKeys = tekCalculations.transformToKeyHistoryInExternalFormat(keys, symptoms)
        Timber.tag(TAG).d("Transformed keys with symptoms %s from %s to %s", symptoms, keys, transformedKeys)

        val checkIns = checkInsRepo.completedCheckIns.first().filter { it.hasSubmissionConsent && !it.isSubmitted }
        val checkInsReport = checkInsTransformer.transform(checkIns, symptoms)

        Timber.tag(TAG).d("Transformed CheckIns from: %s to: %s", checkIns, checkInsReport)

        val payload = SrsSubmissionPayload(
            srsOtp = srsOtp,
            checkInsReport = checkInsReport,
            exposureKeys = transformedKeys,
            visitedCountries = appConfig.getSupportedCountries(),
            submissionType = type.toSubmissionType()
        )

        Timber.tag(TAG).d("Submitting %s", payload)
        playbook.submit(payload)

        Timber.tag(TAG).d("Submission successful, deleting submission data.")
        tekStorage.reset()

        Timber.tag(TAG).d("Marking %d submitted CheckIns.", checkIns.size)
        checkInsRepo.updatePostSubmissionFlags(checkIns)

        submissionReporter.reportAt(timeStamper.nowUTC)

        srsSubmissionSettings.resetOtp()
        Timber.tag(TAG).d("SRS submission finished successfully!")
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun currentOtp(nowUtc: Instant): SrsOtp {
        val srsOtp = srsSubmissionSettings.getOtp()
        return if (srsOtp?.isValid(nowUtc) == true) {
            Timber.tag(TAG).d("SRS otp is still valid -> use it")
            srsOtp
        } else {
            Timber.tag(TAG).d("SRS otp is not valid -> new otp generated")
            SrsOtp()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun attest(
        appConfig: ConfigData,
        srsOtp: SrsOtp,
        androidId: ByteString,
        checkDeviceTime: Boolean = true
    ): AttestationContainer = try {
        val attestRequest = SrsDeviceAttestationRequest(
            configData = appConfig,
            checkDeviceTime = checkDeviceTime,
            scenarioPayload = SRSOneTimePasswordRequestAndroid.SRSOneTimePassword.newBuilder()
                .setOtp(srsOtp.uuid.toString())
                .setAndroidId(androidId)
                .build()
                .toByteArray()
        )
        val attestResult = deviceAttestation.attest(attestRequest) as AttestationContainer
        attestResult.requirePass(appConfig.selfReportSubmission.ppac)
        attestResult
    } catch (e: Exception) {
        Timber.d(e, "attest() failed -> map to SRS error")
        throw when (e) {
            is SafetyNetException -> {
                val errorCode = e.type.toSrsErrorType()
                SrsSubmissionException(
                    errorCode = errorCode,
                    errorArgs = errorCode.errorArgs(appConfig.selfReportSubmission),
                    cause = e
                )
            }

            else -> e
        }
    }

    companion object {
        val TAG = tag<SrsSubmissionRepository>()
    }
}
