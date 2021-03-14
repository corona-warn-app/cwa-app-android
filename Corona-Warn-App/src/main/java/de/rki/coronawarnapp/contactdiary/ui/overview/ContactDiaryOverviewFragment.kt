package de.rki.coronawarnapp.contactdiary.ui.overview

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class ContactDiaryOverviewFragment : Fragment(R.layout.contact_diary_overview_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: ContactDiaryOverviewViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryOverviewFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DiaryOverviewAdapter()

        binding.apply {
            contactDiaryOverviewRecyclerview.adapter = adapter

            setupMenu(toolbar)
            toolbar.setNavigationOnClickListener {
                vm.onBackButtonPress()
            }
        }

        vm.listItems.observe2(this) {
            adapter.update(it)
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

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
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

    private fun setupMenu(toolbar: Toolbar) = toolbar.apply {
        inflateMenu(R.menu.menu_contact_diary_overview)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_contact_diary_information -> {
                    doNavigate(
                        ContactDiaryOverviewFragmentDirections
                            .actionContactDiaryOverviewFragmentToContactDiaryOnboardingFragment(showBottomNav = false)
                    )
                    true
                }
                R.id.menu_contact_diary_export_entries -> {
                    vm.onExportPress()
                    true
                }
                R.id.menu_contact_diary_edit_persons -> {
                    doNavigate(
                        ContactDiaryOverviewFragmentDirections
                            .actionContactDiaryOverviewFragmentToContactDiaryEditPersonsFragment()
                    )
                    true
                }
                R.id.menu_contact_diary_edit_locations -> {
                    doNavigate(
                        ContactDiaryOverviewFragmentDirections
                            .actionContactDiaryOverviewFragmentToContactDiaryEditLocationsFragment()
                    )
                    true
                }
                else -> onOptionsItemSelected(it)
            }
        }
    }
}
