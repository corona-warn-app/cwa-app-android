package de.rki.coronawarnapp.test.api.ui

import de.rki.coronawarnapp.environment.EnvironmentSetup

data class EnvironmentState(
    val current: EnvironmentSetup.Type,
    val available: List<EnvironmentSetup.Type>,
    val urlSubmission: String,
    val urlDownload: String,
    val urlVerification: String
) {
    companion object {
        internal fun EnvironmentSetup.toEnvironmentState() = EnvironmentState(
            current = currentEnvironment,
            available = EnvironmentSetup.Type.values().toList(),
            urlSubmission = submissionCdnUrl,
            urlDownload = downloadCdnUrl,
            urlVerification = verificationCdnUrl
        )
    }
}
