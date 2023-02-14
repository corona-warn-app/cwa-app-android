package de.rki.coronawarnapp.ui.information

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import timber.log.Timber

/**
 * Basic Fragment which links to static and web content.
 */
@AndroidEntryPoint
class InformationFragment : Fragment(R.layout.fragment_information) {

    private val vm: InformationFragmentViewModel by viewModels()

    private val binding: FragmentInformationBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.currentENFVersion.observe(viewLifecycleOwner) {
            binding.informationEnfVersion.apply {
                isGone = it == null
                text = it
            }
        }
        vm.appVersion.observe(viewLifecycleOwner) {
            binding.informationVersion.text = it
        }

        vm.cclConfigVersion.observe(viewLifecycleOwner) {
            binding.cclVersion.text = it
        }

        binding.informationEnfVersion.setOnClickListener {
            try {
                startActivity(Intent(ExposureNotificationClient.ACTION_EXPOSURE_NOTIFICATION_SETTINGS))
            } catch (e: Exception) {
                Timber.e(e, "Can't open ENF settings.")
            }
        }

        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.informationContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.informationAbout.setOnClickListener {
            findNavController().navigate(
                InformationFragmentDirections.actionInformationFragmentToInformationAboutFragment()
            )
        }
        binding.informationPrivacy.setOnClickListener {
            findNavController().navigate(
                InformationFragmentDirections.actionInformationFragmentToInformationPrivacyFragment()
            )
        }
        binding.informationTerms.setOnClickListener {
            findNavController().navigate(
                InformationFragmentDirections.actionInformationFragmentToInformationTermsFragment()
            )
        }
        binding.informationAccessibilityStatement.setOnClickListener {
            openUrl(getString(R.string.accessibility_statement_link))
        }
        binding.informationContact.setOnClickListener {
            findNavController().navigate(
                InformationFragmentDirections.actionInformationFragmentToInformationContactFragment()
            )
        }
        binding.informationLegal.setOnClickListener {
            findNavController().navigate(
                InformationFragmentDirections.actionInformationFragmentToInformationLegalFragment()
            )
        }
        binding.informationTechnical.setOnClickListener {
            findNavController().navigate(
                InformationFragmentDirections.actionInformationFragmentToInformationTechnicalFragment()
            )
        }
        binding.informationDebuglog.setOnClickListener {
            findNavController().navigate(
                InformationFragmentDirections.actionInformationFragmentToDebuglogFragment()
            )
        }
        binding.informationHeader.setNavigationOnClickListener {
            popBackStack()
        }
        binding.informationRelease.setOnClickListener {
            findNavController().navigate(
                InformationFragmentDirections.actionInformationFragmentToNewReleaseInfoFragment(true)
            )
        }
    }
}
