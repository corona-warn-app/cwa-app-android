package de.rki.coronawarnapp.dccticketing.ui.consent.one

import android.annotation.SuppressLint
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
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import java.net.URLEncoder
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DccTicketingConsentOneFragment : Fragment(R.layout.fragment_dcc_ticketing_consent_one), AutoInject {
    private val args by navArgs<DccTicketingConsentOneFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: DccTicketingConsentOneViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccTicketingConsentOneViewModel.Factory
            factory.create(
                dccTicketingTransactionContext = qrcodeSharedViewModel.dccTicketingTransactionContext(
                    args.transactionContextIdentifier
                )
            )
        }
    )
    private val binding: FragmentDccTicketingConsentOneBinding by viewBinding()
    private val qrcodeSharedViewModel by navGraphViewModels<QrcodeSharedViewModel>(R.id.nav_graph)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            toolbar.setNavigationOnClickListener { viewModel.goBack() }
            cancelButton.setOnClickListener { viewModel.goBack() }

            appBarLayout.onOffsetChange { _, subtitleAlpha ->
                headerImage.alpha = subtitleAlpha
            }

            privacyInformation.setOnClickListener {
                findNavController().navigate(R.id.informationPrivacyFragment)
            }
        }

        viewModel.showCloseDialog.observe2(this) {
            showCloseAlertDialog()
        }

        viewModel.uiState.observe2(this) {
            with(binding) {
                provider.text = "\"${it.provider}\""
                subject.text = "\"${it.subject}\""
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { viewModel.goBack() }
    }

    private fun showCloseAlertDialog() {
        DccTicketingDialogType.ConfirmCancelation.show(
            fragment = this,
            negativeButtonAction = { popBackStack() }
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
