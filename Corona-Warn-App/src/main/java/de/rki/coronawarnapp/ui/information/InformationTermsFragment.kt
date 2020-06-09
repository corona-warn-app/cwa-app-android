package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationTermsBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.AssetConstants

/**
 * Basic Fragment which only displays static content.
 */
class InformationTermsFragment : BaseFragment() {
    companion object {
        private val TAG: String? = InformationTermsFragment::class.simpleName
    }

    private var _binding: FragmentInformationTermsBinding? = null
    private val binding: FragmentInformationTermsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationTermsBinding.inflate(inflater)
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
        loadWebView()
    }

    override fun onStart() {
        super.onStart()
        binding.informationTermsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    override fun onResume() {
        super.onResume()
        binding.informationTermsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun setContentDescription() {
        val backButtonString: String = getString(R.string.button_back)
        // TODO contentDescription for back button, should be in XML
    }

    private fun setButtonOnClickListener() {
        binding.informationTermsHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }

    private fun loadWebView() {
        val informationTermsHtmlFilename = getString(R.string.information_terms_html_path)
        binding.informationTermsWebview.loadUrl(AssetConstants.ANDROID_ASSET_PATH + informationTermsHtmlFilename)
    }
}
