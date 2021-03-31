package de.rki.coronawarnapp.util.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield

fun intervalFlow(interval: Long, emitAtZero: Boolean = true) = flow {
    var elapsed = 0L

    if (emitAtZero) emit(elapsed)

    while (true) {
        delay(interval)
        yield()

        elapsed += interval
        emit(elapsed)
    }
}
