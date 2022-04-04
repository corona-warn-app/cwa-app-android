package de.rki.coronawarnapp.util.list

/**
 * Returns True when this collection and the specified collection share at least
 * one common element, otherwise it returns False.
 */
fun <T> Iterable<T>.hasIntersect(other: Iterable<T>): Boolean {
    return intersect(other).isNotEmpty()
}
