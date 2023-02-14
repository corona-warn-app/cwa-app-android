package de.rki.coronawarnapp.dccticketing.ui.consent.two

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingConsentTwoBinding
import de.rki.coronawarnapp.dccticketing.ui.dialog.dccTicketingConfirmCancellationDialog
import de.rki.coronawarnapp.dccticketing.ui.dialog.dccTicketingErrorDialog
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DccTicketingConsentTwoFragment : Fragment(R.layout.fragment_dcc_ticketing_consent_two) {

    @Inject lateinit var factory: DccTicketingConsentTwoViewModel.Factory
    private val args by navArgs<DccTicketingConsentTwoFragmentArgs>()
    private val viewModel: DccTicketingConsentTwoViewModel by assistedViewModel {
        factory.create(
            containerId = args.containerId,
            dccTicketingSharedViewModel = dccTicketingSharedViewModel
        )
    }

    private val binding: FragmentDccTicketingConsentTwoBinding by viewBinding()
    private val certificateAdapter = DccTicketingConsentTwoAdapter()
    private val dccTicketingSharedViewModel:
        DccTicketingSharedViewModel by navGraphViewModels(R.id.dcc_ticketing_nav_graph)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        val onBackAction = { viewModel.goBack() }

        toolbar.setNavigationOnClickListener { onBackAction() }
        cancelButton.setOnClickListener { viewModel.showCancelConfirmationDialog() }
        agreeButton.setOnClickListener { viewModel.onUserConsent() }

        privacyInformation.setOnClickListener {
            viewModel.showPrivacyInformation()
        }

        certificate.apply {
            adapter = certificateAdapter
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.standard_8))
        }

        viewModel.events.observe(viewLifecycleOwner) { handleEvents(it) }

        viewModel.uiState.observe(viewLifecycleOwner) {
            val testPartnerText = "\"${it.testPartner}\""
            val providerText = "\"${it.provider}\""

            testPartner.text = testPartnerText
            provider.text = providerText

            certificateAdapter.update(listOf(it.certificateItem))
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { agreeButton.isLoading = it }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { onBackAction() }
    }

    private fun handleEvents(event: DccTicketingConsentTwoEvent) {
        Timber.d("handleEvents(event=%s)", event)
        when (event) {
            NavigateToHome -> findNavController().navigate(R.id.action_dcc_ticketing_nav_graph_pop)
            NavigateBack -> popBackStack()
            NavigateToValidationResult -> findNavController().navigate(
                DccTicketingConsentTwoFragmentDirections.actionConsentTwoFragmentToValidationResultFragment()
            )

            NavigateToPrivacyInformation -> findNavController().navigate(R.id.informationPrivacyFragment)
            ShowCancelConfirmationDialog -> dccTicketingConfirmCancellationDialog { viewModel.cancel() }
            is ShowErrorDialog -> dccTicketingErrorDialog(event.lazyErrorMessage.get(requireContext()))
        }
    }
}
