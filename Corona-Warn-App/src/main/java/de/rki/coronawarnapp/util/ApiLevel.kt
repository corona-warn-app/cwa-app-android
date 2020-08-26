package de.rki.coronawarnapp.util

import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiLevel constructor(val currentLevel: Int = Build.VERSION.SDK_INT) {

    @Inject
    constructor() : this(Build.VERSION.SDK_INT)

    fun hasAPILevel(level: Int): Boolean = currentLevel >= level
}
