package de.rki.coronawarnapp.util.debug

inline fun <T> measureTimeMillisWithResult(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to (System.currentTimeMillis() - start)
}
