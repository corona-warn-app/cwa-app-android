package de.rki.coronawarnapp.util

import android.os.Build

// Can't be const because that prevents them from being mocked in tests
@Suppress("MayBeConstant")
object BuildVersionWrap {
    val SDK_INT = Build.VERSION.SDK_INT
}

fun BuildVersionWrap.hasAPILevel(level: Int): Boolean = SDK_INT >= level
