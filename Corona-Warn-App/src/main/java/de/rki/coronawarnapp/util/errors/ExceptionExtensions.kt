package de.rki.coronawarnapp.util.errors

fun Throwable.causes(): Sequence<Throwable> = sequence {
    var error = this@causes

    while (true) {
        yield(error)

        error = error.cause ?: break
    }
}
