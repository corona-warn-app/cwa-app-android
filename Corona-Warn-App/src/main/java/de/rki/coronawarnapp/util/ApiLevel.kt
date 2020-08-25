package de.rki.coronawarnapp.util

import android.os.Build

class ApiLevel constructor(apiLevel: Int = Build.VERSION.SDK_INT) {

    val currentLevel = apiLevel

    fun hasAPILevel(level: Int): Boolean = currentLevel >= level
}
