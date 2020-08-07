package de.rki.coronawarnapp.http.playbook

import KeyExportFormat
import de.rki.coronawarnapp.service.submission.KeyType
import de.rki.coronawarnapp.util.formatter.TestResult

interface Playbook {

    suspend fun initialRegistration(
        key: String,
        keyType: KeyType
    ): String /* registration token */

    suspend fun testResult(
        registrationToken: String
    ): TestResult

    suspend fun submission(
        registrationToken: String,
        keys: List<KeyExportFormat.TemporaryExposureKey>
    )

    suspend fun dummy()
}
