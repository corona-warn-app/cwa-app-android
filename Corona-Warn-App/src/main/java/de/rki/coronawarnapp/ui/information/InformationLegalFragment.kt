package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationLegalBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ExternalActionHelper
import java.util.Locale

/**
 * Basic Fragment which only displays static content.
 */
class InformationLegalFragment : Fragment() {
    companion object {
        private val TAG: String? = InformationLegalFragment::class.simpleName
    }

    private var _binding: FragmentInformationLegalBinding? = null
    private val binding: FragmentInformationLegalBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationLegalBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
        setContactFormLink(view)
    }

    private fun setContactFormLink(view: View) {
        if (Locale.getDefault().language == Locale.ENGLISH.language ||
            Locale.getDefault().language == Locale.GERMAN.language) {
            binding.informationLegalContactForm.informationLegalContactForm.setOnClickListener {
                ExternalActionHelper.openUrl(
                    this, requireContext().getString(R.string.information_legal_subtitle_contact_form_link_default))
            }
        } else {
            binding.informationLegalContactForm.informationLegalContactFormEn.setOnClickListener {
                ExternalActionHelper.openUrl(
                    this, requireContext().getString(R.string.information_legal_subtitle_contact_form_link_en))
            }
            binding.informationLegalContactForm.informationLegalContactFormDe.setOnClickListener {
                ExternalActionHelper.openUrl(
                    this, requireContext().getString(R.string.information_legal_subtitle_contact_form_link_de))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.informationLegalContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.informationLegalHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
