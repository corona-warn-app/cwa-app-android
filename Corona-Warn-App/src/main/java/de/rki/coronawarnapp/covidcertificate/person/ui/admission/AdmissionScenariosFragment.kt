package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentAdmissionScenariosBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
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

    private val binding by viewBinding<FragmentAdmissionScenariosBinding>()

    private val admissionScenariosAdapter = AdmissionScenariosAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val materialContainerTransform = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
        }
        sharedElementEnterTransition = materialContainerTransform
        sharedElementReturnTransition = materialContainerTransform
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        admissionScenarios.adapter = admissionScenariosAdapter
        toolbar.setNavigationOnClickListener { popBackStack() }
        viewModel.state.observe(viewLifecycleOwner) {
            toolbar.title = it.title
            admissionScenariosAdapter.update(it.scenarios)
        }
    }
}
