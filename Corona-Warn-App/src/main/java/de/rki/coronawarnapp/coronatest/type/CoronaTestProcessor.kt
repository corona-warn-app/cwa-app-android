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

    suspend fun markProcessing(test: CoronaTest, isProcessing: Boolean): CoronaTest

    suspend fun markViewed(test: CoronaTest): CoronaTest

    suspend fun markBadgeAsViewed(test: CoronaTest): CoronaTest

    suspend fun updateResultNotification(test: CoronaTest, sent: Boolean): CoronaTest

    suspend fun markDccCreated(test: CoronaTest, created: Boolean): CoronaTest

    suspend fun recycle(test: CoronaTest): CoronaTest

    suspend fun restore(test: CoronaTest): CoronaTest

    // key submission
    suspend fun markSubmitted(test: PersonalCoronaTest): PersonalCoronaTest
    suspend fun updateSubmissionConsent(test: PersonalCoronaTest, consented: Boolean): PersonalCoronaTest
}
