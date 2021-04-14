package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestServer
import de.rki.coronawarnapp.coronatest.storage.CoronaTestStorage
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoronaTestRepository @Inject constructor(
    private val storage: CoronaTestStorage,
    private val server: CoronaTestServer,
) {

    val coronaTests: Flow<Set<CoronaTest>> = emptyFlow()

    /**
     * When this returns and there was no exception, the test was registered and a valid registrationToken obtained.
     * Your new test should be available via **coronaTests**.
     */
    suspend fun registerTest(request: CoronaTestQRCode) {
        Timber.tag(TAG).i("registerTest(request=%s)", request)
    }

    suspend fun removeTest(guid: CoronaTestGUID): CoronaTest {
        Timber.tag(TAG).i("removeTest(guid=%s)", guid)

        throw NotImplementedError()
    }

    suspend fun markAsSubmitted(guid: CoronaTestGUID) {
        Timber.tag(TAG).i("markAsSubmitted(guid=%s)", guid)
    }

    /**
     * Passing **null** will refresh all test types.
     */
    fun refresh(type: CoronaTest.Type? = null) {
        Timber.tag(TAG).d("refresh(type=%s)", type)
    }

    companion object {
        const val TAG = "CoronaTestRepo"
    }
}
