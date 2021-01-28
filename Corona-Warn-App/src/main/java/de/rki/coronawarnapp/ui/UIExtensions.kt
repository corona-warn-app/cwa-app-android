package de.rki.coronawarnapp.ui

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

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

/**
 * Similar to [setupWithNavController],but it executes the passed action on item selection
 */
fun BottomNavigationView.setupWithNavController2(
    navController: NavController,
    onItemSelected: () -> Unit
) {
    setupWithNavController(navController)
    setOnNavigationItemSelectedListener { item ->
        onItemSelected()
        NavigationUI.onNavDestinationSelected(item, navController)
    }
}
