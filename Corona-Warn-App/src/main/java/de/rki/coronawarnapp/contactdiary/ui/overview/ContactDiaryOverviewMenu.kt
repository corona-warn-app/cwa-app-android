package de.rki.coronawarnapp.contactdiary.ui.overview

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.doNavigate
import javax.inject.Inject

class ContactDiaryOverviewMenu @Inject constructor(
    private val contactDiaryOverviewFragment: ContactDiaryOverviewFragment
) {
    private val context: Context = contactDiaryOverviewFragment.requireContext()

    private val navController: NavController
        get() = contactDiaryOverviewFragment.findNavController()

    fun showMenuFor(view: View) = PopupMenu(context, view).apply {
        inflate(R.menu.menu_contact_diary_overview)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_contact_diary_information -> { true }
                R.id.menu_contact_diary_export_entries -> { true }
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
    }.show()
}
