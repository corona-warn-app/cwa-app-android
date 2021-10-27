package de.rki.coronawarnapp.reyclebin.coronatest

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

@Deprecated("Probably not needed. Remove later")
class RecycledCoronaTestsStorage @Inject constructor() {

    private val mutex = Mutex()

    suspend fun load(): Set<RecycledCoronaTest> = mutex.withLock {
        Timber.d("load()")

        //TODO

        return emptySet()
    }

    suspend fun save(recycledCoronaTests: Set<RecycledCoronaTest>) = mutex.withLock {
        Timber.d("save(recycledCoronaTests=%s)", recycledCoronaTests)

        // TODO
    }
}
