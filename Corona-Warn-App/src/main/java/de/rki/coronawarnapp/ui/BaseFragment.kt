package de.rki.coronawarnapp.ui

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController

/**
 * Extends fragment to prevent navigation error when the user clicks on two buttons at almost
 * the exact time. All fragments in this application extend BaseFragment.
 *
 * @see Fragment
 */
open class BaseFragment : Fragment() {
    protected fun doNavigate(direction: NavDirections) {
        with(findNavController()) {
            currentDestination?.getAction(direction.actionId)
                ?.let { navigate(direction) }
        }
    }
}
