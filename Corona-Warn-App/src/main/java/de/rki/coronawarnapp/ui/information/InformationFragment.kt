package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ExternalActionHelper

/**
 * Basic Fragment which links to static and web content.
 */
class InformationFragment : Fragment() {
    companion object {
        private val TAG: String? = InformationFragment::class.simpleName
    }

    private var _binding: FragmentInformationBinding? = null
    private val binding: FragmentInformationBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
