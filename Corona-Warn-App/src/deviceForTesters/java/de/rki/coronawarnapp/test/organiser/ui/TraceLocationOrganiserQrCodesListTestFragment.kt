package de.rki.coronawarnapp.test.organiser.ui

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganiserQrCodesListFragmentBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import javax.inject.Inject

class TraceLocationOrganiserQrCodesListTestFragment : Fragment(R.layout.trace_location_organiser_qr_codes_list_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
//    private val vm: ContactDiaryOverviewViewModel by cwaViewModels { viewModelFactory }
    private val binding: TraceLocationOrganiserQrCodesListFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

//    private fun setupMenu(toolbar: Toolbar) = toolbar.apply {
//        inflateMenu(R.menu.menu_contact_diary_overview)
//        setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.menu_contact_diary_information -> {
//                    doNavigate(
//                        ContactDiaryOverviewFragmentDirections
//                            .actionContactDiaryOverviewFragmentToContactDiaryOnboardingFragment(showBottomNav = false)
//                    )
//                    true
//                }
//                R.id.menu_contact_diary_export_entries -> {
//                    vm.onExportPress()
//                    true
//                }
//                R.id.menu_contact_diary_edit_persons -> {
//                    doNavigate(
//                        ContactDiaryOverviewFragmentDirections
//                            .actionContactDiaryOverviewFragmentToContactDiaryEditPersonsFragment()
//                    )
//                    true
//                }
//                R.id.menu_contact_diary_edit_locations -> {
//                    doNavigate(
//                        ContactDiaryOverviewFragmentDirections
//                            .actionContactDiaryOverviewFragmentToContactDiaryEditLocationsFragment()
//                    )
//                    true
//                }
//                else -> onOptionsItemSelected(it)
//            }
//        }
//    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Organiser QR Codes List",
            description = "Check organiser qr codes list",
            targetId = R.id.test_orginser_qr_codes_list_fragment
        )
    }
}
