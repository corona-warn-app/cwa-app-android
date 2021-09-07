package de.rki.coronawarnapp.util.ui

import androidx.annotation.IdRes
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat

fun BottomNavigationView.updateCountBadge(@IdRes badgeId: Int, count: Int) {
    if (count > 0) {
        getOrCreateBadge(badgeId).apply {
            number = count
            backgroundColor = context.getColorCompat(R.color.badgeColor)
            badgeTextColor = context.getColorCompat(android.R.color.white)
        }
    } else {
        removeBadge(badgeId)
    }
}
