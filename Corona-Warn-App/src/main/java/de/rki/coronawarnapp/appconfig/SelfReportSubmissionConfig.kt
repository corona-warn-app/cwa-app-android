package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import java.time.Duration

interface SelfReportSubmissionConfig {
    val common: SelfReportSubmissionCommon

    val ppac: SafetyNetRequirements

    interface Mapper : ConfigMapper<SelfReportSubmissionConfig>
}

interface SelfReportSubmissionCommon {
    val timeSinceOnboardingInHours: Duration
    val timeBetweenSubmissionsInDays: Duration
}

data class SelfReportSubmissionCommonContainer(
    override val timeSinceOnboardingInHours: Duration = DEFAULT_HOURS,
    override val timeBetweenSubmissionsInDays: Duration = DEFAULT_DAYS
) : SelfReportSubmissionCommon {
    companion object {
        val DEFAULT_HOURS: Duration = Duration.ofHours(24)
        val DEFAULT_DAYS: Duration = Duration.ofDays(90)
    }
}

data class SelfReportSubmissionConfigContainer(
    override val common: SelfReportSubmissionCommon,
    override val ppac: SafetyNetRequirements
) : SelfReportSubmissionConfig {
    companion object {
        val DEFAULT = SelfReportSubmissionConfigContainer(
            common = SelfReportSubmissionCommonContainer(),
            ppac = SafetyNetRequirementsContainer()
        )
    }
}
