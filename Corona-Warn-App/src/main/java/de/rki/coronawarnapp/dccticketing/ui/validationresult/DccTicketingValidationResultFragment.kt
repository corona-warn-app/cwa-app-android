package de.rki.coronawarnapp.dccticketing.ui.validationresult

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingValidationResultBinding
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class DccTicketingValidationResultFragment : Fragment(R.layout.fragment_dcc_ticketing_validation_result), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    @Inject lateinit var validationResultAdapter: ValidationResultAdapter

    private val dccTicketingSharedViewModel:
        DccTicketingSharedViewModel by navGraphViewModels(R.id.dcc_ticketing_nav_graph)
    private val binding: FragmentDccTicketingValidationResultBinding by viewBinding()
    private val resultViewModel: DccTicketingValidationResultViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccTicketingValidationResultViewModel.Factory
            factory.create(
                dccTicketingSharedViewModel = dccTicketingSharedViewModel
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            validationResultFragments.apply {
                populateList(ticketingAdapter = validationResultAdapter)
                setOnClickListener { resultViewModel.onCloseClicked() }
                offsetChange()
            }

            buttonDone.setOnClickListener { resultViewModel.onDoneClicked() }
        }

        resultViewModel.uiStateFlow.observe(viewLifecycleOwner) {
            binding.validationResultFragments.setHeaderForState(it.result)
            validationResultAdapter.update(it.listItems)
        }

        resultViewModel.navigation.observe(viewLifecycleOwner) {
            handleNavigation(it)
        }
    }

    private fun handleNavigation(navigation: DccTicketingValidationNavigation) {
        when (navigation) {
            DccTicketingValidationNavigation.Close,
            DccTicketingValidationNavigation.Done ->
                findNavController().navigate(R.id.action_dcc_ticketing_nav_graph_pop)
        }
    }
}
