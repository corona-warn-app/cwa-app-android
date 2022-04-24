package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationContactBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.callPhone
import de.rki.coronawarnapp.util.linkifyPhoneNumbers
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * Basic Fragment which only displays static content.
 */
class InformationContactFragment : Fragment(R.layout.fragment_information_contact) {

    private val binding: FragmentInformationContactBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        binding.informationContactBodyOther.linkifyPhoneNumbers()
    }

    override fun onResume() {
        super.onResume()
        binding.informationContactContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            informationContactNavigationRowPhone.navigationRow.setOnClickListener {
                callPhone(getString(R.string.information_contact_phone_call_number))
            }
            informationContactNavigationRowInternationalPhone.navigationRow.setOnClickListener {
                callPhone(getString(R.string.information_contact_button_international_phone))
            }
        }
    }
}
