package de.rki.coronawarnapp.test.api.ui

import de.rki.coronawarnapp.environment.EnvironmentSetup

data class EnvironmentState(
    val isAlternative: Boolean,
    val current: EnvironmentSetup.Type,
    val urlSubmission: String,
    val urlDownload: String,
    val urlVerification: String
) {
    companion object {
        internal fun EnvironmentSetup.toEnvironmentState() = EnvironmentState(
            isAlternative = currentEnvironment != defaultEnvironment,
            current = currentEnvironment,
            urlSubmission = submissionCdnUrl,
            urlDownload = downloadCdnUrl,
            urlVerification = verificationCdnUrl
        )
    }
}
