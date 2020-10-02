package de.rki.coronawarnapp.test.api.ui

import de.rki.coronawarnapp.environment.EnvironmentSetup

data class EnvironmentState(
    val current: EnvironmentSetup.EnvType,
    val available: List<EnvironmentSetup.EnvType>,
    val urlSubmission: String,
    val urlDownload: String,
    val urlVerification: String
) {
    companion object {
        internal fun EnvironmentSetup.toEnvironmentState() = EnvironmentState(
            current = currentEnvironment,
            available = EnvironmentSetup.EnvType.values().toList(),
            urlSubmission = submissionCdnUrl,
            urlDownload = downloadCdnUrl,
            urlVerification = verificationCdnUrl
        )
    }
}
