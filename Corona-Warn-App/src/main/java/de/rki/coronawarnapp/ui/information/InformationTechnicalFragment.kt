package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationTechnicalBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity

/**
 * Basic Fragment which only displays static content.
 */
class InformationTechnicalFragment : BaseFragment() {
    companion object {
        private val TAG: String? = InformationTechnicalFragment::class.simpleName
    }

    private var _binding: FragmentInformationTechnicalBinding? = null
    private val binding: FragmentInformationTechnicalBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationTechnicalBinding.inflate(inflater)
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
        binding.informationTechnicalContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    override fun onResume() {
        super.onResume()
        binding.informationTechnicalContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun setContentDescription() {
        val backButtonString: String = getString(R.string.button_back)
        binding.informationTechnicalHeader.headerToolbar.setNavigationContentDescription(backButtonString)
    }

    private fun setButtonOnClickListener() {
        binding.informationTechnicalHeader.headerToolbar.setNavigationOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
