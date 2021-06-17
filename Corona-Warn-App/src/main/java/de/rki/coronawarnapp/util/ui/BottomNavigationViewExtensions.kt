package de.rki.coronawarnapp.util.ui

import androidx.annotation.IdRes
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat

fun BottomNavigationView.updateCountBadge(@IdRes badgeId: Int, count: Int) {
    if (count > 0) {
        val badge = getOrCreateBadge(badgeId)
        badge.number = count
        badge.badgeTextColor = context.getColorCompat(android.R.color.white)
    } else {
        removeBadge(badgeId)
    }
}
