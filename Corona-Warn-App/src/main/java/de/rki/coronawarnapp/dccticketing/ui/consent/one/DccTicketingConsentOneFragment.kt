package de.rki.coronawarnapp.dccticketing.ui.consent.one

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingConsentOneBinding
import de.rki.coronawarnapp.dccticketing.ui.dialog.DccTicketingDialogType
import de.rki.coronawarnapp.dccticketing.ui.dialog.show
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import java.net.URLEncoder
import javax.inject.Inject

class DccTicketingConsentOneFragment : Fragment(R.layout.fragment_dcc_ticketing_consent_one), AutoInject {
    private val args by navArgs<DccTicketingConsentOneFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: DccTicketingConsentOneViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccTicketingConsentOneViewModel.Factory
            factory.create(
                dccTicketingSharedViewModel = dccTicketingSharedViewModel,
                qrcodeSharedViewModel = qrcodeSharedViewModel,
                args.transactionContextIdentifier
            )
        }
    )
    private val binding: FragmentDccTicketingConsentOneBinding by viewBinding()
    private val qrcodeSharedViewModel by navGraphViewModels<QrcodeSharedViewModel>(R.id.nav_graph)
    private val dccTicketingSharedViewModel:
        DccTicketingSharedViewModel by navGraphViewModels(R.id.dcc_ticketing_nav_graph)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        val onUserCancelAction = { viewModel.onUserCancel() }

        toolbar.setNavigationOnClickListener { onUserCancelAction() }
        cancelButton.setOnClickListener { onUserCancelAction() }
        agreeButton.setOnClickListener { viewModel.onUserConsent() }

        appBarLayout.onOffsetChange { _, subtitleAlpha ->
            headerImage.alpha = subtitleAlpha
        }

        privacyInformation.setOnClickListener {
            viewModel.showPrivacyInformation()
        }

        viewModel.events.observe2(this@DccTicketingConsentOneFragment) {
            handleEvents(it)
        }

        viewModel.uiState.observe2(this@DccTicketingConsentOneFragment) {
            val providerText = "\"${it.provider}\""
            val subjectText = "\"${it.subject}\""

            provider.text = providerText
            subject.text = subjectText
        }

        viewModel.isLoading.observe2(this@DccTicketingConsentOneFragment) {
            agreeButton.isLoading = it
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { onUserCancelAction() }
    }

    private fun handleEvents(event: DccTicketingConsentOneEvent) {
        Timber.d("handleEvents(event=%s)", event)
        when (event) {
            NavigateBack -> popBackStack()
            NavigateToCertificateSelection ->
                findNavController().navigate(
                    DccTicketingConsentOneFragmentDirections
                        .actionDccTicketingConsentOneFragmentToDccTicketingCertificateSelectionFragment()
                )

            NavigateToPrivacyInformation -> findNavController().navigate(R.id.informationPrivacyFragment)
            ShowCancelConfirmationDialog -> showCloseDialog()
            is ShowErrorDialog -> showErrorDialog(lazyErrorMessage = event.lazyErrorMessage)
        }
    }

    private fun showCloseDialog() {
        DccTicketingDialogType.ConfirmCancellation.show(
            fragment = this,
            negativeButtonAction = { viewModel.goBack() }
        )
    }

    private fun showErrorDialog(lazyErrorMessage: LazyString) {
        val msg = lazyErrorMessage.get(requireContext())
        DccTicketingDialogType.ErrorDialog(msg = msg).show(
            fragment = this
        )
    }

    companion object {
        fun uri(
            transactionContextIdentifier: String
        ): Uri {
            val encodedTransactionContextId = URLEncoder.encode(transactionContextIdentifier, "UTF-8")
            return "cwa://dcc.ticketing.consent.one/?transactionContextIdentifier=$encodedTransactionContextId".toUri()
        }
    }
}
