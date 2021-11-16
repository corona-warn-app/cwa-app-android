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
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class DccTicketingConsentOneFragment : Fragment(R.layout.fragment_dcc_ticketing_consent_one) {

    private val binding: FragmentDccTicketingConsentOneBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            toolbar.setNavigationOnClickListener { showCloseAlertDialog() }
            cancelButton.setOnClickListener { showCloseAlertDialog() }

            appBarLayout.onOffsetChange { _, subtitleAlpha ->
                headerImage.alpha = subtitleAlpha
            }

            privacyInformation.setOnClickListener {
                findNavController().navigate(R.id.informationPrivacyFragment)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { showCloseAlertDialog() }
    }

    private fun showCloseAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dcc_ticketing_consent_one_cancel_dialog_title)
            .setMessage(R.string.dcc_ticketing_consent_one_cancel_dialog_body)
            .setNegativeButton(R.string.dcc_ticketing_consent_one_cancel_dialog_continue_btn) { _, _ -> }
            .setPositiveButton(R.string.dcc_ticketing_consent_one_cancel_dialog_cancel_btn) { _, _ -> popBackStack() }
            .show()
    }
}
