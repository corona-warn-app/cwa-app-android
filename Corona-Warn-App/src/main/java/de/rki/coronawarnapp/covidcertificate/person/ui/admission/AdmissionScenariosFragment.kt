package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentAdmissionScenariosBinding
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

@AndroidEntryPoint
class AdmissionScenariosFragment : Fragment(R.layout.fragment_admission_scenarios) {

    private val admissionScenariosSharedViewModel by navGraphViewModels<AdmissionScenariosSharedViewModel>(
        R.id.covid_certificates_graph
    )

    @Inject lateinit var factory: AdmissionScenariosViewModel.Factory
    private val viewModel: AdmissionScenariosViewModel by assistedViewModel {
        factory.create(admissionScenariosSharedViewModel)
    }

    private val binding by viewBinding<FragmentAdmissionScenariosBinding>()
    private val blockingDialog by lazy { AdmissionBlockingDialog(requireContext()) }
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

        viewModel.calculationState.observe(viewLifecycleOwner) { calculationState ->
            when (calculationState) {
                AdmissionScenariosViewModel.Calculating -> blockingDialog.setState(true)
                AdmissionScenariosViewModel.CalculationDone -> {
                    blockingDialog.setState(false)
                    popBackStack()
                }
            }
        }
    }
}
