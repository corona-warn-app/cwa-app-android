package de.rki.coronawarnapp.util

import kotlin.math.min

fun String.trimToLength(maxLength: Int) = this.substring(0, min(length, maxLength))
