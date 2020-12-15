package de.rki.coronawarnapp.contactdiary.ui

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import de.rki.coronawarnapp.R
import javax.inject.Inject

class ContactDiaryMenu @Inject constructor(
    private val context: Context,
    private val navController: NavController,
    private val navDirections: NavDirections
) {

    fun showMenuFor(view: View) = PopupMenu(context, view).apply {
        inflate(R.menu.menu_contact_diary)
        setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.menu_edit_locations -> {
                    // todo
                    true
                }
                R.id.menu_edit_persons -> {
                    // todo
                    true
                }
                R.id.menu_information -> {
                    // todo
                    true
                }
                else -> false
            }
        }
    }.show()
}
