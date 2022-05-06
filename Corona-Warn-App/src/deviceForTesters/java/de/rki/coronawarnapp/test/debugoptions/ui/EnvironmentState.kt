package de.rki.coronawarnapp.test.debugoptions.ui

import de.rki.coronawarnapp.environment.EnvironmentSetup

data class EnvironmentState(
    val current: EnvironmentSetup.Type,
    val available: List<EnvironmentSetup.Type>,
    val urlSubmission: String,
    val urlDownload: String,
    val urlVerification: String,
    val urlDataDonation: String,
    val urlLogUpload: String,
    val pubKeyCrowdNotifier: String,
    val pubKeyAppConfig: String,
    val dccServerUrl: String,
    val dccReissuanceServerUrl: String,
    val isOverwritten: Boolean,
) {
    companion object {
        internal fun EnvironmentSetup.toEnvironmentState() = EnvironmentState(
            current = currentEnvironment,
            available = EnvironmentSetup.Type.values().toList(),
            urlSubmission = submissionCdnUrl,
            urlDownload = downloadCdnUrl,
            urlVerification = verificationCdnUrl,
            urlDataDonation = dataDonationCdnUrl,
            urlLogUpload = logUploadServerUrl,
            pubKeyCrowdNotifier = crowdNotifierPublicKey,
            pubKeyAppConfig = appConfigPublicKey,
            dccServerUrl = dccServerUrl,
            dccReissuanceServerUrl = dccReissuanceServerUrl,
            isOverwritten = launchEnvironment != null
        )
    }
}
