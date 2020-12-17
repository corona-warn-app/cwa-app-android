package de.rki.coronawarnapp.contactdiary.ui.overview

import android.os.Bundle
import android.view.View
import androidx.core.app.ShareCompat
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
import timber.log.Timber
import javax.inject.Inject

class ContactDiaryOverviewFragment : Fragment(R.layout.contact_diary_overview_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    @Inject lateinit var contactDiaryOverviewMenu: ContactDiaryOverviewMenu
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
        vm.exportLocationsAndPersons.observe2(this) {
            exportLocationsAndPersons(it)
        }
    }

    private fun exportLocationsAndPersons(exportString: String) {
        Timber.d("exportLocationsAndPersons(exportString=$exportString)")
        activity?.let { activity ->
            val shareIntent = ShareCompat.IntentBuilder
                .from(activity)
                .setType("text/plain")
                .setSubject(getString(R.string.contact_diary_export_subject))
                .setText(exportString)
                .createChooserIntent()

            if (shareIntent.resolveActivity(activity.packageManager) != null) {
                startActivity(shareIntent)
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
