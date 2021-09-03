package de.rki.coronawarnapp

inline fun <reified T> tag(): String = T::class.simpleName.toString()
