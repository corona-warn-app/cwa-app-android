package de.rki.coronawarnapp.test.contactdiary.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestContactDiaryBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class ContactDiaryTestFragment : Fragment(R.layout.fragment_test_contact_diary), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: ContactDiaryTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestContactDiaryBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.locationVisits.observe2(this) {
            binding.locationVisitsFancy.text = vm.getFancyLocationVisitString(it)
            binding.locationVisitsStatus.text = vm.getLocationVisitStatusString(it)
        }

        vm.personEncounters.observe2(this) {
            binding.personEncountersFancy.text = vm.getFancyPersonEncounterString(it)
            binding.personEncountersStatus.text = vm.getPersonEncounterStatusString(it)
        }

        binding.apply {
            wipeAllButton.setOnClickListener { vm.clearAll() }
            outdatedLocationVisitsButton.setOnClickListener { vm.createLocationVisit(true) }
            normalLocationVisitsButton.setOnClickListener { vm.createLocationVisit(false) }
            outdatedPersonEncountersButton.setOnClickListener { vm.createPersonEncounters(true) }
            normalPersonEncountersButton.setOnClickListener { vm.createPersonEncounters(false) }
            locationVisitsCleanButton.setOnClickListener { vm.clearLocationVisits() }
            personEncountersCleanButton.setOnClickListener { vm.clearPersonEncounters() }
        }
    }

    companion object {
        val TAG: String = ContactDiaryTestFragment::class.simpleName!!
        val MENU_ITEM = TestMenuItem(
            title = "Contact Diary Test Options",
            description = "Contact Diary related test options..",
            targetId = R.id.test_contact_diary_fragment
        )
    }
}
