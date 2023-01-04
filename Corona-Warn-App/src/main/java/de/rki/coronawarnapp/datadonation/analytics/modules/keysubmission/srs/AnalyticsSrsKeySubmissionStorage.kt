package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.srs

import javax.inject.Singleton

@Singleton
class AnalyticsSrsKeySubmissionStorage {
    suspend fun reset() {
    }

    suspend fun saveSrsPpaData(srsPpaData: String) {
    }

    suspend fun getSrsPpaData(): String? {
        return null
    }
}
