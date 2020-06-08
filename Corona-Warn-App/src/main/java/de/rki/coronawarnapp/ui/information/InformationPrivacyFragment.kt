package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationPrivacyBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity

/**
 * Basic Fragment which only displays static content.
 */
class InformationPrivacyFragment : BaseFragment() {
    companion object {
        private val TAG: String? = InformationPrivacyFragment::class.simpleName
    }

    private var _binding: FragmentInformationPrivacyBinding? = null
    private val binding: FragmentInformationPrivacyBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationPrivacyBinding.inflate(inflater)
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
        binding.informationPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    override fun onResume() {
        super.onResume()
        binding.informationPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun setContentDescription() {
        val backButtonString: String = getString(R.string.button_back)
        binding.informationPrivacyHeader.headerToolbar.setNavigationContentDescription(backButtonString)
    }

    private fun setButtonOnClickListener() {
        binding.informationPrivacyHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
