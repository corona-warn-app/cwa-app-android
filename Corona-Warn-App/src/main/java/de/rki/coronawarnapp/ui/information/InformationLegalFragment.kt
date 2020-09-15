package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationLegalBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.ui.viewLifecycle

/**
 * Basic Fragment which only displays static content.
 */
class InformationLegalFragment : Fragment() {
    companion object {
        private val TAG: String? = InformationLegalFragment::class.simpleName
    }

    private var binding: FragmentInformationLegalBinding by viewLifecycle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInformationLegalBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
        setUpContactFormLinks()
    }

    /**
     * Make the links clickable and convert to hyperlink
     */
    private fun setUpContactFormLinks() {
        binding.informationLegalContactForm.informationLegalContactForm
            .convertToHyperlink(getString(R.string.information_legal_subtitle_contact_url))
        binding.informationLegalContactForm.informationLegalContactFormNonEnDe
            .movementMethod = LinkMovementMethod.getInstance()
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
