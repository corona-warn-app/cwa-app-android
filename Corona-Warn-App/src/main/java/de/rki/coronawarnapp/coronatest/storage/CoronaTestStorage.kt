package de.rki.coronawarnapp.coronatest.storage

import android.content.Context
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoronaTestStorage @Inject constructor(
    @AppContext private val context: Context,
) {

    suspend fun load(): Set<CoronaTest> {
        Timber.tag(TAG).d("load()")
        throw NotImplementedError()
    }

    suspend fun save(tests: Set<CoronaTest>) {
        Timber.tag(TAG).d("save(tests=%s)", tests)
    }

    companion object {
        private const val TAG = "CoronaTestStorage"
    }
}
