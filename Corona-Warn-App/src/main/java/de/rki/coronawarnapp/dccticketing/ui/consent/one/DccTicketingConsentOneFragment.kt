package de.rki.coronawarnapp.dccticketing.ui.consent.one

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingConsentOneBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DccTicketingConsentOneFragment : Fragment(R.layout.fragment_dcc_ticketing_consent_one), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: DccTicketingConsentOneViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentDccTicketingConsentOneBinding by viewBinding()

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { viewModel.goBack() }
    }

    private fun showCloseAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dcc_ticketing_consent_one_cancel_dialog_title)
            .setMessage(R.string.dcc_ticketing_consent_one_cancel_dialog_body)
            .setPositiveButton(R.string.dcc_ticketing_consent_one_cancel_dialog_continue_btn) { _, _ -> }
            .setNegativeButton(R.string.dcc_ticketing_consent_one_cancel_dialog_cancel_btn) { _, _ -> popBackStack() }
            .show()
    }
}
