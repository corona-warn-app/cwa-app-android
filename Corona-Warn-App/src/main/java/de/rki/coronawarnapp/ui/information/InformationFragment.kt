package de.rki.coronawarnapp.ui.information

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.setGone
import de.rki.coronawarnapp.util.ui.viewBindingLazy
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

    private val binding: FragmentInformationBinding by viewBindingLazy()

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

        binding.informationEnfVersion.setOnClickListener {
            try {
                startActivity(Intent(ExposureNotificationClient.ACTION_EXPOSURE_NOTIFICATION_SETTINGS))
            } catch (e: Exception) {
                Timber.e(e, "Can't open ENF settings.")
            }
        }

        setButtonOnClickListener()
        setAccessibilityDelegate()
    }

    override fun onResume() {
        super.onResume()
        binding.informationContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setAccessibilityDelegate() {
        val accessibilityDelegate: View.AccessibilityDelegate =
            object : View.AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(v: View?, info: AccessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(v, info)
                    val string: String = getString(R.string.information_help_title_accessibility)
                    info.text = string
                }
            }
        binding.informationHelp.mainRowItemSubtitle.accessibilityDelegate = accessibilityDelegate
    }

    private fun setButtonOnClickListener() {
        binding.informationAbout.mainRow.setOnClickListener {
            findNavController().doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationAboutFragment()
            )
        }
        binding.informationPrivacy.mainRow.setOnClickListener {
            findNavController().doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationPrivacyFragment()
            )
        }
        binding.informationTerms.mainRow.setOnClickListener {
            findNavController().doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationTermsFragment()
            )
        }
        binding.informationContact.mainRow.setOnClickListener {
            findNavController().doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationContactFragment()
            )
        }
        binding.informationHelp.mainRow.setOnClickListener {
            ExternalActionHelper.openUrl(this, requireContext().getString(R.string.main_about_link))
        }
        binding.informationLegal.mainRow.setOnClickListener {
            findNavController().doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationLegalFragment()
            )
        }
        binding.informationTechnical.mainRow.setOnClickListener {
            findNavController().doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationTechnicalFragment()
            )
        }
        binding.informationHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
