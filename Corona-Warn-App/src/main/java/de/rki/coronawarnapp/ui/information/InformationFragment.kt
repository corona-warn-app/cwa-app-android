package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.databinding.FragmentInformationBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.UiConstants
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.OpenUrlHelper

/**
 * Basic Fragment which links to static and web content.
 */
class InformationFragment : BaseFragment() {
    companion object {
        private val TAG: String? = InformationFragment::class.simpleName
    }

    private lateinit var binding: FragmentInformationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInformationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setButtonOnClickListener()
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
            OpenUrlHelper.navigate(this, UiConstants.INFORMATION_URI)
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
