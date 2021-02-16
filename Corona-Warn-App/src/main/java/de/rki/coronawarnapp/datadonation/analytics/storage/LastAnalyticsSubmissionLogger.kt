package de.rki.coronawarnapp.datadonation.analytics.storage

import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData

interface LastAnalyticsSubmissionLogger {
    suspend fun storeAnalyticsData(analyticsProto: PpaData.PPADataAndroid)

    suspend fun getLastAnalyticsData(): LastAnalyticsSubmission?
}
