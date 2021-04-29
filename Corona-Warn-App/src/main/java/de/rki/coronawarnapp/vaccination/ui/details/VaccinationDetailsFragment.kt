package de.rki.coronawarnapp.vaccination.ui.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentVaccinationDetailsBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import javax.inject.Inject

class VaccinationDetailsFragment : Fragment(R.layout.fragment_vaccination_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<VaccinationDetailsFragmentArgs>()
    private val binding: FragmentVaccinationDetailsBinding by viewBindingLazy()
    private val viewModel: VaccinationDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as VaccinationDetailsViewModel.Factory
            factory.create(
                certificateId = args.certificateId,
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            deleteButton.setOnClickListener { viewModel.deleteVaccination() }

            viewModel.vaccinationCertificate.observe(viewLifecycleOwner) {
                name.text = it.run { "$firstName $lastName" }
                birthDate.text = getString(
                    R.string.vaccination_details_birth_date,
                    it.dateOfBirth.toString(format)
                )
                vaccinatedAt.text = it.vaccinatedAt.toString(format)
                vaccinationName.text = it.vaccinationName
                vaccinationManufacturer.text = it.vaccinationManufacturer
                chargeId.text = it.chargeId
                certificateIssuer.text = it.certificateIssuer
                certificateCountry.text = it.certificateCountry.getLabel(requireContext())
                certificateId.text = it.certificateId
            }
        }

    companion object {
        private val format = DateTimeFormat.shortDate()
    }
}
