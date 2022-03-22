package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationLegalBinding
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * Basic Fragment which only displays static content.
 */
class InformationLegalFragment : Fragment(R.layout.fragment_information_legal) {

    private val binding: FragmentInformationLegalBinding by viewBinding()

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
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
    }
}
