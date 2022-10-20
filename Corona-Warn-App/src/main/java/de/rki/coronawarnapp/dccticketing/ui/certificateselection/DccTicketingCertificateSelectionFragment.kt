package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingCertificateSelectionBinding
import de.rki.coronawarnapp.dccticketing.ui.dialog.dccTicketingConfirmCancellationDialog
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class DccTicketingCertificateSelectionFragment :
    Fragment(R.layout.fragment_dcc_ticketing_certificate_selection),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: FragmentDccTicketingCertificateSelectionBinding by viewBinding()
    private val dccTicketingSharedViewModel by navGraphViewModels<DccTicketingSharedViewModel>(
        R.id.dcc_ticketing_nav_graph
    )
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
            toolbar.setNavigationOnClickListener { viewModel.onUserCancel() }
            recyclerView.apply {
                adapter = certificatesAdapter
                addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            }
        }
        viewModel.apply {
            events.observe2(this@DccTicketingCertificateSelectionFragment) { event -> navigate(event) }
            items.observe2(this@DccTicketingCertificateSelectionFragment) { certificatesAdapter.update(it) }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) { viewModel.onUserCancel() }
    }

    private fun navigate(event: DccTicketingCertificateSelectionEvents) {
        when (event) {
            ShowCancelConfirmationDialog -> dccTicketingConfirmCancellationDialog { viewModel.closeScreen() }
            CloseSelectionScreen -> popBackStack()
            is NavigateToConsentTwoFragment -> findNavController().navigate(
                DccTicketingCertificateSelectionFragmentDirections
                    .actionDccTicketingCertificateSelectionFragmentToDccTicketingConsentTwoFragment(
                        event.selectedCertificateContainerId
                    )
            )
        }
    }
}
