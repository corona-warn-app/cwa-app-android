package de.rki.coronawarnapp.datadonation.analytics.storage

import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import javax.inject.Inject

class DefaultLastAnalyticsSubmissionLogger @Inject constructor() : LastAnalyticsSubmissionLogger {
    override suspend fun storeAnalyticsData(analyticsProto: PpaData.PPADataAndroid) {
        // Do not store past analytics submissions in Production
    }

    override suspend fun getLastAnalyticsData(): LastAnalyticsSubmission? = null
}
