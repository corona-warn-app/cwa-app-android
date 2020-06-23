package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationContactBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ExternalActionHelper

/**
 * Basic Fragment which only displays static content.
 */
class InformationContactFragment : Fragment() {
    companion object {
        private val TAG: String? = InformationContactFragment::class.simpleName
    }

    private var _binding: FragmentInformationContactBinding? = null
    private val binding: FragmentInformationContactBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationContactBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.informationContactContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.informationContactHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.informationContactNavigationRowPhone.navigationRow.setOnClickListener {
            val number = getString(R.string.information_contact_phone_call_number)
            ExternalActionHelper.call(this, number)
        }
    }
}
