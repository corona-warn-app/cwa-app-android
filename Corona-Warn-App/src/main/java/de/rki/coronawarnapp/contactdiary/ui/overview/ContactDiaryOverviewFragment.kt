package de.rki.coronawarnapp.contactdiary.ui.overview

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.ContactDiaryOverviewAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ContactDiaryOverviewFragment : Fragment(R.layout.contact_diary_overview_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: ContactDiaryOverviewViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryOverviewFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ContactDiaryOverviewAdapter {
            vm.onItemPress(it)
        }

        setupToolbar()

        binding.apply {
            contactDiaryOverviewRecyclerview.adapter = adapter

            contactDiaryOverviewHeader.contactDiaryHeaderButtonBack.buttonIcon.setOnClickListener {
                vm.onBackButtonPress()
            }
        }

        vm.listItems.observe2(this) {
            adapter.setItems(it)
        }

        vm.routeToScreen.observe2(this) {
            when (it) {
                ContactDiaryOverviewNavigationEvents.NavigateToMainActivity -> {
                    requireActivity().finish()
                }

                is ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment -> {
                    doNavigate(
                        ContactDiaryOverviewFragmentDirections
                            .actionContactDiaryOverviewFragmentToContactDiaryDayFragment(it.localDateString)
                    )
                }
            }
        }
    }

    private fun setupToolbar() {

        binding.contactDiaryOverviewHeader.contactDiaryHeaderOptionsMenu.buttonIcon.apply {
            contentDescription = getString(R.string.button_menu)
            setOnClickListener { showMenuFor(it) }
        }
    }

    private fun showMenuFor(view: View) = PopupMenu(context, view).apply {
        inflate(R.menu.menu_contact_diary_overview)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_contact_diary_information -> {
                    true
                }
                R.id.menu_contact_diary_export_entries -> {
                    vm.onExportPress()
                    true
                }
                R.id.menu_contact_diary_edit_persons -> {
                    true
                }
                R.id.menu_contact_diary_edit_locations -> {
                    true
                }
                else -> onOptionsItemSelected(it)
            }
        }
    }.show()
}
