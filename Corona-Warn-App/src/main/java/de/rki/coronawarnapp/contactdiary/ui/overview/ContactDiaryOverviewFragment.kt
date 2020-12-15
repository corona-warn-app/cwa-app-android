package de.rki.coronawarnapp.contactdiary.ui.overview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryActivity
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.ContactDiaryOverviewAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ContactDiaryOverviewFragment : Fragment(R.layout.contact_diary_overview_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: ContactDiaryOverviewViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryOverviewFragmentBinding by viewBindingLazy()

    @Inject lateinit var contactDiaryOverviewMenu: ContactDiaryOverviewMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ContactDiaryOverviewAdapter {
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
                    (requireActivity() as ContactDiaryActivity).finish()
                }
            }
        }
    }

    private fun setupToolbar() {

        binding.contactDiaryOverviewHeader.contactDiaryHeaderOptionsMenu.buttonIcon.apply {
            contentDescription = getString(R.string.button_menu)
            setOnClickListener { contactDiaryOverviewMenu.showMenuFor(it) }
        }
    }
}
