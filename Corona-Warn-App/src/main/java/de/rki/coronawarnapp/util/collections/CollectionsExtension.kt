package de.rki.coronawarnapp.util.collections

/**
 * Clear all existing items and add the new ones
 */
fun <E> MutableCollection<E>.replaceAll(elements: Collection<E>) {
    clear()
    addAll(elements)
}
