package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class AdmissionScenariosFragment : Fragment(R.layout.fragment_admission_scenarios), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val admissionViewModel by navGraphViewModels<AdmissionSharedViewModel>(R.id.covid_certificates_graph)
    private val viewModel: AdmissionScenariosViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as AdmissionScenariosViewModel.Factory
            factory.create(
                admissionSharedViewModel = admissionViewModel
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.admissionCheckScenarios.observe(viewLifecycleOwner) {
            // TODO: Bind views
        }
    }
}
