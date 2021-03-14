package de.rki.coronawarnapp.util.debug

inline fun <T> measureTimeMillisWithResult(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to (System.currentTimeMillis() - start)
}

inline fun <T> measureTime(onMeasured: (Long) -> Unit, block: () -> T): T {
    val start = System.currentTimeMillis()
    try {
        return block()
    } finally {
        val stop = System.currentTimeMillis()
        onMeasured(stop - start)
    }
}
