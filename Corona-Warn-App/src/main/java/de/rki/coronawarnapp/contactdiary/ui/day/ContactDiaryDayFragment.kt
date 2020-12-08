package de.rki.coronawarnapp.contactdiary.ui.day

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.adapter.ContactDiaryDayFragmentsAdapter
import de.rki.coronawarnapp.contactdiary.util.registerOnPageChangeCallback
import de.rki.coronawarnapp.databinding.ContactDiaryDayFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.format.DateTimeFormat
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

    private val dateFormat by lazy {
        DateTimeFormat.forPattern("EEEE, dd.MM.yy")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.contactDiaryDayViewPager.adapter =
            ContactDiaryDayFragmentsAdapter(this, viewModel.contactDiaryTabs)

        binding.contactDiaryDayViewPager.registerOnPageChangeCallback {
            viewModel.updateCurrentTab(it)
        }

        binding.contactDiaryDayFab.setOnClickListener {
            viewModel.onCreateButtonClicked()
        }

        TabLayoutMediator(binding.contactDiaryDayTabLayout, binding.contactDiaryDayViewPager) { tab, position ->
            tab.text = viewModel.contactDiaryTabs[position].tabName
        }.attach()

        viewModel.currentTab.observe2(this) {
            binding.contactDiaryDayFab.text = it.fabText
        }

        viewModel.displayedDay.observe2(this) {
            binding.contactDiaryDayHeader.title = it.toString(dateFormat)
        }
    }
}
