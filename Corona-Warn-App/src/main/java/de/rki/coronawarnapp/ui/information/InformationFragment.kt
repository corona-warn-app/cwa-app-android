package de.rki.coronawarnapp.ui.information

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.fragment.app.Fragment
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.setGone
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

/**
 * Basic Fragment which links to static and web content.
 */
class InformationFragment : Fragment(R.layout.fragment_information), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: InformationFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentInformationBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.currentENFVersion.observe2(this) {
            binding.informationEnfVersion.apply {
                setGone(it == null)
                text = it
            }
        }
        vm.appVersion.observe2(this) {
            binding.informationVersion.text = it
        }

        vm.cclConfigVersion.observe2(this) {
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
        binding.informationAbout.mainRow.setOnClickListener {
            doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationAboutFragment()
            )
        }
        binding.informationPrivacy.mainRow.setOnClickListener {
            doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationPrivacyFragment()
            )
        }
        binding.informationTerms.mainRow.setOnClickListener {
            doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationTermsFragment()
            )
        }
        binding.informationAccessibilityStatement.mainRow.setOnClickListener {
            openUrl(getString(R.string.accessibility_statement_link))
        }
        binding.informationContact.mainRow.setOnClickListener {
            doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationContactFragment()
            )
        }
        binding.informationLegal.mainRow.setOnClickListener {
            doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationLegalFragment()
            )
        }
        binding.informationTechnical.mainRow.setOnClickListener {
            doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationTechnicalFragment()
            )
        }
        binding.informationDebuglog.mainRow.setOnClickListener {
            doNavigate(
                InformationFragmentDirections.actionInformationFragmentToDebuglogFragment()
            )
        }
        binding.informationHeader.setNavigationOnClickListener {
            popBackStack()
        }
        binding.informationRelease.mainRow.setOnClickListener {
            doNavigate(
                InformationFragmentDirections.actionInformationFragmentToNewReleaseInfoFragment(true)
            )
        }
    }
}
