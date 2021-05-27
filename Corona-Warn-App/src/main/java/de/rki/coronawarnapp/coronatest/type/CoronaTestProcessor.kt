package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN

interface CoronaTestProcessor {

    val type: CoronaTest.Type

    suspend fun create(request: CoronaTestQRCode): CoronaTest

    suspend fun create(request: CoronaTestTAN): CoronaTest

    suspend fun pollServer(test: CoronaTest): CoronaTest

    /**
     * Called before a test is removed.
     */
    suspend fun onRemove(toBeRemoved: CoronaTest)

    suspend fun markSubmitted(test: CoronaTest): CoronaTest

    suspend fun markProcessing(test: CoronaTest, isProcessing: Boolean): CoronaTest

    suspend fun markViewed(test: CoronaTest): CoronaTest

    suspend fun updateSubmissionConsent(test: CoronaTest, consented: Boolean): CoronaTest

    suspend fun updateDccConsent(test: CoronaTest, consented: Boolean): CoronaTest

    suspend fun updateResultNotification(test: CoronaTest, sent: Boolean): CoronaTest
}
