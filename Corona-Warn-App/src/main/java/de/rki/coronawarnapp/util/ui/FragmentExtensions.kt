package de.rki.coronawarnapp.util.ui

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.ui.doNavigate
import timber.log.Timber

fun Fragment.doNavigate(direction: NavDirections) = findNavController().doNavigate(direction)

fun Fragment.popBackStack(): Boolean {
    if (!isAdded) {
        IllegalStateException("Fragment is not added").also {
            Timber.w(it, "Trying to pop backstack on Fragment that isn't added to an Activity.")
        }
        return false
    }
    return findNavController().popBackStack()
}
