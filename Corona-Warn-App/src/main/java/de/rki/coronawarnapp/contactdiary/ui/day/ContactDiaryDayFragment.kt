package de.rki.coronawarnapp.contactdiary.ui.day

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.ContactDiaryDayFragmentsAdapter
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.ContactDiaryDayTab
import de.rki.coronawarnapp.contactdiary.util.registerOnPageChangeCallback
import de.rki.coronawarnapp.databinding.ContactDiaryDayFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryDayFragment : Fragment(R.layout.contact_diary_day_fragment), AutoInject {
    private val binding: ContactDiaryDayFragmentBinding by viewBindingLazy()

    private val navArgs by navArgs<ContactDiaryDayFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryDayViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryDayViewModel.Factory
            factory.create(navArgs.selectedDay)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactDiaryTabs = listOf(ContactDiaryDayTab.PersonTab, ContactDiaryDayTab.LocationTab)

        val adapter = ContactDiaryDayFragmentsAdapter(this, contactDiaryTabs, navArgs.selectedDay)

        binding.contactDiaryDayViewPager.adapter = adapter

        TabLayoutMediator(binding.contactDiaryDayTabLayout, binding.contactDiaryDayViewPager) { tab, position ->
            val tabSource = adapter.tabs[position]
            tab.setText(tabSource.tabNameResource)
        }.attach()

        binding.apply {
            contactDiaryDayViewPager.registerOnPageChangeCallback {
                binding.contactDiaryDayFab.setText(adapter.tabs[it].fabTextResource)
            }

            contactDiaryDayFab.setOnClickListener {
                viewModel.onCreateButtonClicked(adapter.tabs[contactDiaryDayTabLayout.selectedTabPosition])
            }

            contactDiaryDayHeader.setNavigationOnClickListener {
                viewModel.onBackPressed()
            }
        }

        viewModel.uiState.observe2(this) { uiState ->
            binding.contactDiaryDayHeader.apply {
                title = uiState.dayText(context)
                contentDescription = uiState.dayTextContentDescription(context)
            }
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                ContactDiaryDayNavigationEvents.NavigateToOverviewFragment -> popBackStack()
                ContactDiaryDayNavigationEvents.NavigateToAddPersonBottomSheet -> doNavigate(
                    ContactDiaryDayFragmentDirections
                        .actionContactDiaryDayFragmentToContactDiaryPersonBottomSheetDialogFragment(
                            addedAt = navArgs.selectedDay
                        )
                )
                ContactDiaryDayNavigationEvents.NavigateToAddLocationBottomSheet -> doNavigate(
                    ContactDiaryDayFragmentDirections
                        .actionContactDiaryDayFragmentToContactDiaryLocationBottomSheetDialogFragment(
                            addedAt = navArgs.selectedDay
                        )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }
}
