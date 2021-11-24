package de.rki.coronawarnapp.dccticketing.ui.consent.two

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingConsentTwoBinding
import de.rki.coronawarnapp.dccticketing.ui.dialog.DccTicketingDialogType
import de.rki.coronawarnapp.dccticketing.ui.dialog.show
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class DccTicketingConsentTwoFragment : Fragment(R.layout.fragment_dcc_ticketing_consent_two), AutoInject {

    private val args by navArgs<DccTicketingConsentTwoFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: DccTicketingConsentTwoViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccTicketingConsentTwoViewModel.Factory
            factory.create(
                dccTicketingTransactionContext = args.transactionContext,
                containerId = args.containerId
            )
        }
    )
    private val binding: FragmentDccTicketingConsentTwoBinding by viewBinding()
    private val certificateAdapter = DccTicketingConsentTwoAdapter()

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

            privacyInformation.setOnClickListener {
                findNavController().navigate(R.id.informationPrivacyFragment)
            }
            certificate.apply {
                adapter = certificateAdapter
//                addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            }
        }

        viewModel.showCloseDialog.observe2(this) {
            showCloseAlertDialog()
        }

        viewModel.uiState.observe2(this) {
            with(binding) {
                testPartner.text = "\"${it.testPartner}\""
                provider.text = "\"${it.provider}\""
            }
            certificateAdapter.update(listOf(it.certificateItem))
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { viewModel.goBack() }
    }

    private fun showCloseAlertDialog() {
        DccTicketingDialogType.ConfirmCancelation.show(
            fragment = this,
            negativeButtonAction = { popBackStack() }
        )
    }
}
