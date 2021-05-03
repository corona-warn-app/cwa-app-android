package de.rki.coronawarnapp.vaccination.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestVaccinationBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class VaccinationTestFragment : Fragment(R.layout.fragment_test_vaccination), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: VaccinationTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestVaccinationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.openIncompleteVaccinationList.setOnClickListener {
            doNavigate(
                VaccinationTestFragmentDirections.actionVaccinationTestFragmentToVaccinationListFragment(
                    "vaccinated-person-incomplete"
                )
            )
        }

        binding.openCompleteVaccinationList.setOnClickListener {
            doNavigate(
                VaccinationTestFragmentDirections.actionVaccinationTestFragmentToVaccinationListFragment(
                    "vaccinated-person-complete"
                )
            )
        }

        binding.openVaccinationDetailsIncomplete.setOnClickListener {
            doNavigate(
                VaccinationTestFragmentDirections
                    .actionVaccinationTestFragmentToVaccinationDetailsFragment(
                        "05930482748454836478695764787840"
                    )
            )
        }

        binding.openVaccinationDetailsComplete.setOnClickListener {
            doNavigate(
                VaccinationTestFragmentDirections
                    .actionVaccinationTestFragmentToVaccinationDetailsFragment(
                        "05930482748454836478695764787841"
                    )
            )
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Vaccination",
            description = "View & Control vaccination related features.",
            targetId = R.id.vaccinationTestFragment
        )
    }
}
