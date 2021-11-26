package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingCertificateSelectionBinding
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class DccTicketingCertificateSelectionFragment :
    Fragment(R.layout.fragment_dcc_ticketing_certificate_selection), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val binding: FragmentDccTicketingCertificateSelectionBinding by viewBinding()
    private val dccTicketingSharedViewModel:
        DccTicketingSharedViewModel by navGraphViewModels(R.id.dcc_ticketing_nav_graph)
    private val viewModel: DccTicketingCertificateSelectionViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccTicketingCertificateSelectionViewModel.Factory
            factory.create(dccTicketingSharedViewModel = dccTicketingSharedViewModel)
        }
    )
    private val certificatesAdapter = DccTicketingCertificateSelectionAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { viewModel.closeScreen() }
            recyclerView.apply {
                adapter = certificatesAdapter
                addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            }
            viewModel.events.observe(viewLifecycleOwner) { event ->
                when (event) {
                    is CloseSelectionScreen -> popBackStack()
                    is NavigateToConsentTwoFragment -> doNavigate(
                        DccTicketingCertificateSelectionFragmentDirections
                            .actionDccTicketingCertificateSelectionFragmentToDccTicketingConsentTwoFragment(
                                event.selectedCertificateContainerId
                            )
                    )
                }
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            certificatesAdapter.update(state.certificateItems)
        }
    }
}
