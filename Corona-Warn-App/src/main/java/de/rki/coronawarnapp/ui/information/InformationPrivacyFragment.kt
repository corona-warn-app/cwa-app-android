package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationPrivacyBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * Basic Fragment which only displays static content.
 */
class InformationPrivacyFragment : Fragment(R.layout.fragment_information_privacy) {
    private val binding: FragmentInformationPrivacyBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
    }

    override fun onResume() {
        super.onResume()
        binding.informationPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
