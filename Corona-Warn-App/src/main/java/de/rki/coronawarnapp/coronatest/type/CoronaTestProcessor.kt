package de.rki.coronawarnapp.coronatest.type

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest

interface CoronaTestProcessor {

    val type: CoronaTest.Type

    suspend fun create(request: TestRegistrationRequest): CoronaTest

    suspend fun pollServer(test: CoronaTest): CoronaTest

    /**
     * Called before a test is removed.
     */
    suspend fun onRemove(toBeRemoved: CoronaTest)

    suspend fun markSubmitted(test: CoronaTest): CoronaTest

    suspend fun markProcessing(test: CoronaTest, isProcessing: Boolean): CoronaTest

    suspend fun markViewed(test: CoronaTest): CoronaTest

    suspend fun updateSubmissionConsent(test: CoronaTest, consented: Boolean): CoronaTest

    suspend fun updateResultNotification(test: CoronaTest, sent: Boolean): CoronaTest
}
