package de.rki.coronawarnapp.ui.main.home

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.CWADebug
import javax.inject.Inject

class HomeMenu @Inject constructor(
    private val homeFragment: HomeFragment
) {
    private val context: Context = homeFragment.requireContext()

    private val navController: NavController
        get() = homeFragment.findNavController()

    fun showMenuFor(view: View) = PopupMenu(context, view).apply {
        inflate(R.menu.menu_main)
        menu.findItem(R.id.menu_test).isVisible = CWADebug.isDeviceForTestersBuild
        setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.menu_help -> {
                    navController.doNavigate(HomeFragmentDirections.actionMainFragmentToMainOverviewFragment())
                    true
                }
                R.id.menu_information -> {
                    navController.doNavigate(HomeFragmentDirections.actionMainFragmentToInformationFragment())
                    true
                }
                R.id.menu_settings -> {
                    navController.doNavigate(HomeFragmentDirections.actionMainFragmentToSettingsFragment())
                    true
                }
                R.id.menu_test -> {
                    navController.doNavigate(HomeFragmentDirections.actionMainFragmentToTestNavGraph())
                    true
                }
                R.id.menu_crashreporter -> {
                    navController.doNavigate(HomeFragmentDirections.actionMainFragmentToCrashReportFragment())
                    true
                }
                else -> homeFragment.onOptionsItemSelected(it)
            }
        }
    }.show()
}
