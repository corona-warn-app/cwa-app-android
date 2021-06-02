package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class PcrTeleTanCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): BugCensor.CensorContainer? = mutex.withLock {
        if (transientTeleTans.isEmpty()) return null

        var container = BugCensor.CensorContainer(message)

        transientTeleTans.forEach {
            container = container.censor(it, PLACEHOLDER + it.takeLast(3))
        }

        return container.nullIfEmpty()
    }

    companion object {
        private val mutex = Mutex()
        private val transientTeleTans = mutableSetOf<String>()
        suspend fun addTan(tan: String) = mutex.withLock {
            transientTeleTans.add(tan)
        }

        suspend fun clearTans() = mutex.withLock {
            transientTeleTans.clear()
        }

        private const val PLACEHOLDER = "#######"
    }
}
