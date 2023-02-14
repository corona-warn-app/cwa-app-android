package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingCertificateSelectionBinding
import de.rki.coronawarnapp.dccticketing.ui.dialog.dccTicketingConfirmCancellationDialog
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class DccTicketingCertificateSelectionFragment : Fragment(R.layout.fragment_dcc_ticketing_certificate_selection) {

    @Inject lateinit var factory: DccTicketingCertificateSelectionViewModel.Factory

    private val binding: FragmentDccTicketingCertificateSelectionBinding by viewBinding()
    private val dccTicketingSharedViewModel by navGraphViewModels<DccTicketingSharedViewModel>(
        R.id.dcc_ticketing_nav_graph
    )
    private val viewModel: DccTicketingCertificateSelectionViewModel by assistedViewModel {
        factory.create(dccTicketingSharedViewModel = dccTicketingSharedViewModel)
    }

    private val certificatesAdapter = DccTicketingCertificateSelectionAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { viewModel.onUserCancel() }
            recyclerView.apply {
                adapter = certificatesAdapter
                addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.standard_8))
            }
        }
        viewModel.apply {
            events.observe(viewLifecycleOwner) { event -> navigate(event) }
            items.observe(viewLifecycleOwner) { certificatesAdapter.update(it) }
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
