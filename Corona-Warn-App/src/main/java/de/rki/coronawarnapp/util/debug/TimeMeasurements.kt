package de.rki.coronawarnapp.util.debug

/**
 * Executes the given [block] and returns elapsed time as Pair<Result,Time>
 */
inline fun <T> measureTimeMillisWithResult(block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to (System.currentTimeMillis() - start)
}
