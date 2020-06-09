package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.OpenUrlHelper

/**
 * Basic Fragment which links to static and web content.
 */
class InformationFragment : BaseFragment() {
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
        setContentDescription()
    }

    override fun onStart() {
        super.onStart()
        binding.informationContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    override fun onResume() {
        super.onResume()
        binding.informationContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun setContentDescription() {
        val backButtonString: String = getString(R.string.button_back)
        // TODO contentDescription for back button, should be in XML
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
        binding.informationContact.mainRow.setOnClickListener {
            doNavigate(
                InformationFragmentDirections.actionInformationFragmentToInformationContactFragment()
            )
        }
        binding.informationHelp.mainRow.setOnClickListener {
            OpenUrlHelper.navigate(this, requireContext().getString(R.string.main_about_link))
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
        binding.informationHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
