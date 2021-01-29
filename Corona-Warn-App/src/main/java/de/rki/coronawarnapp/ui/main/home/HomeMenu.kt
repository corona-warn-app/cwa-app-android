package de.rki.coronawarnapp.ui.main.home

import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.CWADebug
import javax.inject.Inject

class HomeMenu @Inject constructor(
    private val homeFragment: HomeFragment
) {

    private val navController: NavController
        get() = homeFragment.findNavController()

    fun setupMenu(toolbar: Toolbar) = toolbar.apply {
        inflateMenu(R.menu.menu_main)
        menu.findItem(R.id.menu_test).isVisible = CWADebug.isDeviceForTestersBuild
        menu.findItem(R.id.menu_share).setShowAsAction(SHOW_AS_ACTION_ALWAYS)
        setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.menu_share -> {
                    navController.doNavigate(HomeFragmentDirections.actionMainFragmentToMainSharingFragment())
                    true
                }
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
                else -> homeFragment.onOptionsItemSelected(it)
            }
        }
    }
}
