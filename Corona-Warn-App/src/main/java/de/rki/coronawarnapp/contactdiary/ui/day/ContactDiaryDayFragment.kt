package de.rki.coronawarnapp.contactdiary.ui.day

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.ContactDiaryDayFragmentsAdapter
import de.rki.coronawarnapp.contactdiary.util.registerOnPageChangeCallback
import de.rki.coronawarnapp.databinding.ContactDiaryDayFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
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

        binding.contactDiaryDayViewPager.adapter =
            ContactDiaryDayFragmentsAdapter(this, viewModel.contactDiaryTabs, navArgs.selectedDay)

        TabLayoutMediator(binding.contactDiaryDayTabLayout, binding.contactDiaryDayViewPager) { tab, position ->
            val tabSource = viewModel.contactDiaryTabs[position]
            tab.text = resources.getString(tabSource.tabNameResource)
        }.attach()

        binding.apply {
            contactDiaryDayViewPager.registerOnPageChangeCallback {
                viewModel.updateCurrentTab(it)
            }

            contactDiaryDayFab.setOnClickListener {
                viewModel.onCreateButtonClicked()
            }
        }

        viewModel.uiState.observe2(this) {
            binding.contactDiaryDayHeader.title = it.dayText
            binding.contactDiaryDayFab.text = resources.getString(it.fabTextResource)
        }

        viewModel.createPerson.observe2(this) {
            doNavigate(
                ContactDiaryDayFragmentDirections
                    .actionContactDiaryDayFragmentToContactDiaryPersonBottomSheetDialogFragment()
            )
        }

        viewModel.createLocation.observe2(this) {
            doNavigate(
                ContactDiaryDayFragmentDirections
                    .actionContactDiaryDayFragmentToContactDiaryLocationBottomSheetDialogFragment()
            )
        }
    }
}
