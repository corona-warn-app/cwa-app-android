package de.rki.coronawarnapp.http.playbook

import KeyExportFormat
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.util.formatter.TestResult

class PlaybookImpl : Playbook {
    override suspend fun initialRegistration(key: String, keyType: KeyType): String {
        // register
        // dummy
        // dummy
        TODO("Not yet implemented")
    }

    override suspend fun testResult(registrationToken: String): TestResult {
        // check
        // dummy
        // dummy
        TODO("Not yet implemented")
    }

    override suspend fun submission(
        registrationToken: String,
        keys: List<KeyExportFormat.TemporaryExposureKey>
    ) {
        // tan
        // dummy
        // upload
        TODO("Not yet implemented")
    }

    override suspend fun dummy() {
        // dummy
        // dummy
        // dummy
        TODO("Not yet implemented")
    }
}
