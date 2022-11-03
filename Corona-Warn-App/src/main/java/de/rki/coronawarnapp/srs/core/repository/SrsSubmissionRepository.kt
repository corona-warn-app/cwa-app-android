package de.rki.coronawarnapp.srs.core.repository

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.getSupportedCountries
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsTransformer
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload.SubmissionType
import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtp.SRSOneTimePassword
import de.rki.coronawarnapp.srs.core.AndroidIdProvider
import de.rki.coronawarnapp.srs.core.model.SrsAttestationResult
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationRequest
import de.rki.coronawarnapp.srs.core.model.SrsDeviceAttestationRequest
import de.rki.coronawarnapp.srs.core.model.SrsOtp
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionPayload
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.core.playbook.SrsPlaybook
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.submission.task.ExposureKeyHistoryCalculations
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

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
    private val androidIdProvider: AndroidIdProvider,
) {

    suspend fun submit(
        type: SrsSubmissionType,
        symptoms: Symptoms = Symptoms.NO_INFO_GIVEN
    ) {

        val appConfig = appConfigProvider.getAppConfig()
        val storedOtp = srsSubmissionSettings.getOtp()
        val nowUtc = timeStamper.nowUTC
        var srsOtp = if (storedOtp?.isValid(nowUtc) == true) storedOtp else SrsOtp()
        val attestRequest = SrsDeviceAttestationRequest(
            configData = appConfig,
            scenarioPayload = SRSOneTimePassword.newBuilder().setOtp(srsOtp.uuid.toString()).build().toByteArray()
        )

        val attestResult = deviceAttestation.attest(
            request = attestRequest
        ) { salt, report ->
            SrsAttestationResult(ourSalt = salt, report = report)
        } as SrsAttestationResult

        attestResult.requirePass(appConfig.selfReportSubmission.ppac)

        if (!srsOtp.isValid(nowUtc)) {
            val authResponse = playbook.authorize(
                SrsAuthorizationRequest(
                    srsOtp = srsOtp,
                    safetyNetJws = attestResult.report.jwsResult,
                    salt = String(attestResult.ourSalt),
                    androidId = androidIdProvider.getAndroidId()
                )
            )

            srsOtp = srsOtp.copy(expiresAt = authResponse.expiresAt)
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
    }

    companion object {
        val TAG = tag<SrsSubmissionRepository>()
    }
}

internal fun SrsSubmissionType.toSubmissionType() = when (this) {
    SrsSubmissionType.SRS_SELF_TEST -> SubmissionType.SUBMISSION_TYPE_SRS_SELF_TEST
    SrsSubmissionType.SRS_RAT -> SubmissionType.SUBMISSION_TYPE_SRS_RAT
    SrsSubmissionType.SRS_REGISTERED_PCR -> SubmissionType.SUBMISSION_TYPE_SRS_REGISTERED_PCR
    SrsSubmissionType.SRS_UNREGISTERED_PCR -> SubmissionType.SUBMISSION_TYPE_SRS_UNREGISTERED_PCR
    SrsSubmissionType.SRS_RAPID_PCR -> SubmissionType.SUBMISSION_TYPE_SRS_RAPID_PCR
    SrsSubmissionType.SRS_OTHER -> SubmissionType.SUBMISSION_TYPE_SRS_OTHER
}
