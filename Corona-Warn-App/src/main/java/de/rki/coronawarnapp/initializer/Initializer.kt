package de.rki.coronawarnapp.initializer

import timber.log.Timber
import javax.inject.Inject

interface Initializer {
    fun initialize()
}

class Initializers @Inject constructor(
    private val initializers: Set<@JvmSuppressWildcards Initializer>
) {
    operator fun invoke() {
        Timber.d("Setup [${initializers.size}] Initializers")
        initializers.forEach { initializer ->
            Timber.d("initialize => %s", initializer::class.simpleName)
            initializer.initialize()
        }
    }
}
