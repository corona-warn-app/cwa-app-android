package de.rki.coronawarnapp.vaccination.ui.list

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentVaccinationListBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class VaccinationListFragment : Fragment(R.layout.fragment_vaccination_list), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<VaccinationListFragmentArgs>()
    private val binding: FragmentVaccinationListBinding by viewBindingLazy()
    private val viewModel: VaccinationListViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as VaccinationListViewModel.Factory
            factory.create(
                vaccinatedPersonIdentifier = args.vaccinatedPersonId
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }

            viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
                bindUiState(uiState)
            }

            registerNewVaccinationButton.setOnClickListener {
                Toast.makeText(requireContext(), "TODO \uD83D\uDEA7", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun FragmentVaccinationListBinding.bindUiState(uiState: VaccinationListViewModel.UiState) = with(uiState) {
        nameCardTitle.text = fullName
        nameCardSubtitle.text = getString(
            R.string.vaccination_list_name_card_subtitle,
            dayOfBirth
        )

        vaccinationCardTitle.text = getString(
            R.string.vaccination_list_vaccination_card_title,
            vaccinations.first().doseNumber,
            vaccinations.first().totalSeriesOfDoses
        )

        vaccinationCardSubtitle.text = getString(
            R.string.vaccination_list_vaccination_card_subtitle,
            vaccinations.first().vaccinatedAt
        )
    }
}
