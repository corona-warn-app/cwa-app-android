package de.rki.coronawarnapp.contactdiary.ui.day

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.Hold
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.ContactDiaryDayFragmentsAdapter
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.ContactDiaryDayTab
import de.rki.coronawarnapp.contactdiary.ui.location.ContactDiaryAddLocationFragmentArgs
import de.rki.coronawarnapp.contactdiary.ui.person.ContactDiaryAddPersonFragmentArgs
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.contactdiary.util.registerOnPageChangeCallback
import de.rki.coronawarnapp.databinding.ContactDiaryDayFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryDayFragment : Fragment(R.layout.contact_diary_day_fragment), AutoInject {
    private val binding: ContactDiaryDayFragmentBinding by viewBinding()

    private val navArgs by navArgs<ContactDiaryDayFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryDayViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryDayViewModel.Factory
            factory.create(navArgs.selectedDay)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
    }

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
                binding.contactDiaryDayFab.text = getString(adapter.tabs[it].fabTextResource)
                binding.contactDiaryDayFab.contentDescription = getString(adapter.tabs[it].fabTextResourceAccessibility)
                // Extend FAB when on page change
                onScrollChange(true)
            }

            contactDiaryDayFab.setOnClickListener {
                viewModel.onCreateButtonClicked(adapter.tabs[contactDiaryDayTabLayout.selectedTabPosition])
            }

            contactDiaryDayHeader.setNavigationOnClickListener {
                view.hideKeyboard()
                viewModel.onBackPressed()
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            binding.contactDiaryDayHeader.apply {
                title = uiState.dayText(context)
                contentDescription = uiState.dayTextContentDescription(context)
            }
        }

        viewModel.routeToScreen.observe(viewLifecycleOwner) {
            when (it) {
                ContactDiaryDayNavigationEvents.NavigateToOverviewFragment -> popBackStack()
                ContactDiaryDayNavigationEvents.NavigateToAddPersonFragment ->
                    findNavController().navigate(
                        R.id.action_contactDiaryDayFragment_to_contactDiaryAddPersonFragment,
                        ContactDiaryAddPersonFragmentArgs(addedAt = navArgs.selectedDay).toBundle(),
                        null,
                        FragmentNavigatorExtras(
                            binding.contactDiaryDayFab to binding.contactDiaryDayFab.transitionName
                        )
                    )

                ContactDiaryDayNavigationEvents.NavigateToAddLocationFragment ->
                    findNavController().navigate(
                        R.id.action_contactDiaryDayFragment_to_contactDiaryAddLocationFragment,
                        ContactDiaryAddLocationFragmentArgs(addedAt = navArgs.selectedDay).toBundle(),
                        null,
                        FragmentNavigatorExtras(
                            binding.contactDiaryDayFab to binding.contactDiaryDayFab.transitionName
                        )
                    )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    fun onScrollChange(extend: Boolean) =
        with(binding.contactDiaryDayFab) {
            if (extend) extend() else shrink()
        }
}
