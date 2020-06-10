package de.rki.coronawarnapp.ui

import androidx.navigation.NavController
import androidx.navigation.NavDirections

/**
 * Extends NavController to prevent navigation error when the user clicks on two buttons at almost
 * the exact time.
 *
 * @see [NavController]
 */
fun NavController.doNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)
        ?.let { navigate(direction) }
}
