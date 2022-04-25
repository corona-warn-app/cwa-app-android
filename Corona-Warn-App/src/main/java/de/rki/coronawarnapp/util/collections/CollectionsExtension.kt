package de.rki.coronawarnapp.util.collections

/**
 * Clear all existing items and add the new ones
 */
fun <E> MutableCollection<E>.replaceAll(elements: Collection<E>) {
    clear()
    addAll(elements)
}

/**
 * Groups elements of the original collection by the key returned by the given [keySelector] function
 * applied to each element and returns a map where each group key is associated with a list of corresponding elements.
 * Any element for which the given [keySelector] returns null will be dropped.
 */
inline fun <T, K : Any> Iterable<T>.groupByNotNull(
    keySelector: (T) -> K?
): Map<K, List<T>> = buildMap<K, MutableList<T>> {
    for (item in this@groupByNotNull) {
        val key = keySelector(item) ?: continue
        val list = getOrPut(key) { mutableListOf() }
        list.add(item)
    }
}
