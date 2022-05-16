package de.rki.coronawarnapp.initializer

import timber.log.Timber
import javax.inject.Inject

interface Initializer {
    fun initialize()
}

class Initializers @Inject constructor(
    @JvmSuppressWildcards
    private val initializers: Set<Initializer>
) {
    operator fun invoke() {
        Timber.d("Setup [${initializers.size}] Initializers")
        initializers.forEach { initializer ->
            initializer.initialize()
        }
    }
}
