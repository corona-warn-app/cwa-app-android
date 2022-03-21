package de.rki.coronawarnapp.CoronaTest.type

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest

interface CoronaTestProcessor {

    val type: CoronaTest.Type

    suspend fun create(request: TestRegistrationRequest): PersonalCoronaTest

    suspend fun pollServer(test: PersonalCoronaTest): PersonalCoronaTest

    /**
     * Called before a test is removed.
     */
    suspend fun onRemove(toBeRemoved: PersonalCoronaTest)

    suspend fun markProcessing(test: PersonalCoronaTest, isProcessing: Boolean): PersonalCoronaTest

    suspend fun markViewed(test: PersonalCoronaTest): PersonalCoronaTest

    suspend fun markBadgeAsViewed(test: PersonalCoronaTest): PersonalCoronaTest

    suspend fun updateResultNotification(test: PersonalCoronaTest, sent: Boolean): PersonalCoronaTest

    suspend fun markDccCreated(test: PersonalCoronaTest, created: Boolean): PersonalCoronaTest

    suspend fun recycle(test: PersonalCoronaTest): PersonalCoronaTest

    suspend fun restore(test: PersonalCoronaTest): PersonalCoronaTest

    suspend fun markSubmitted(test: PersonalCoronaTest): PersonalCoronaTest
    suspend fun updateSubmissionConsent(test: PersonalCoronaTest, consented: Boolean): PersonalCoronaTest
}
