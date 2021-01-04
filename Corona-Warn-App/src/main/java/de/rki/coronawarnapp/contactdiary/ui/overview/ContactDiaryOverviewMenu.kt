package de.rki.coronawarnapp.contactdiary.ui.overview

import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

// TODO(Remove this useless class)
class ContactDiaryOverviewMenu @Inject constructor(
    private val contactDiaryOverviewFragment: ContactDiaryOverviewFragment
) {

    private val navController: NavController
        get() = contactDiaryOverviewFragment.findNavController()
    private val vm: ContactDiaryOverviewViewModel by contactDiaryOverviewFragment.cwaViewModels {
        contactDiaryOverviewFragment.viewModelFactory }

    fun setupMenu(toolbar: Toolbar) = toolbar.apply {
        inflateMenu(R.menu.menu_contact_diary_overview)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_contact_diary_information -> {
                    navController.doNavigate(
                        ContactDiaryOverviewFragmentDirections
                            .actionContactDiaryOverviewFragmentToContactDiaryOnboardingFragment()
                    )
                    true
                }
                R.id.menu_contact_diary_export_entries -> { vm.onExportPress(context)
                    true }
                R.id.menu_contact_diary_edit_persons -> {
                    navController.doNavigate(
                        ContactDiaryOverviewFragmentDirections
                            .actionContactDiaryOverviewFragmentToContactDiaryEditPersonsFragment())
                    true
                }
                R.id.menu_contact_diary_edit_locations -> {
                    navController.doNavigate(
                        ContactDiaryOverviewFragmentDirections
                            .actionContactDiaryOverviewFragmentToContactDiaryEditLocationsFragment())
                    true
                }
                else -> contactDiaryOverviewFragment.onOptionsItemSelected(it)
            }
        }
    }
}
