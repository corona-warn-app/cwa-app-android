package de.rki.coronawarnapp.util.ui

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.ui.doNavigate

fun Fragment.doNavigate(direction: NavDirections) = findNavController().doNavigate(direction)
