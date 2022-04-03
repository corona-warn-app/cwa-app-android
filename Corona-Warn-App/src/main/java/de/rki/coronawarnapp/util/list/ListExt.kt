package de.rki.coronawarnapp.util.list

/**
 * Returns True when this collection and the specified collection share at least
 * one common element, otherwise it returns False.
 */
fun <T> Iterable<T>.hasIntersect(other: Iterable<T>): Boolean {
    return intersect(other.toSet()).isNotEmpty()
}

inline fun <C> C.ifEmptyDo(doBlock: (C) -> Unit): C where C : Collection<*> {
    if (isEmpty()) doBlock(this)
    return this
}

inline fun <C> C.ifNotEmptyDo(doBlock: (C) -> Unit): C where C : Collection<*> {
    if (isNotEmpty()) doBlock(this)
    return this
}
