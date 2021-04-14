package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode

interface CoronaTestProcessor {

    val type: CoronaTest.Type

    suspend fun create(data: CoronaTestQRCode): CoronaTest

    suspend fun pollServer(test: CoronaTest): CoronaTest

    /**
     * Called before a test is removed.
     */
    suspend fun onRemove(toBeRemoved: CoronaTest)

    suspend fun markSubmitted(test: CoronaTest): CoronaTest

    suspend fun markProcessing(test: CoronaTest, isProcessing: Boolean): CoronaTest
}
