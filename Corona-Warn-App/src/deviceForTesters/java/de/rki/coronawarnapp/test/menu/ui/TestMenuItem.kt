package de.rki.coronawarnapp.test.menu.ui

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import de.rki.coronawarnapp.R

data class TestMenuItem(
    @DrawableRes val iconRes: Int = R.drawable.ic_bug,
    val title: String,
    val description: String,
    @IdRes val targetId: Int
)
