package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationTechnicalBinding
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.AssetConstants

/**
 * Basic Fragment which only displays static content.
 */
class InformationTechnicalFragment : Fragment() {
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
        loadWebView()
    }

    override fun onStart() {
        super.onStart()
        binding.informationTechnicalContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    override fun onResume() {
        super.onResume()
        binding.informationTechnicalContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun setButtonOnClickListener() {
        binding.informationTechnicalHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }

    private fun loadWebView() {
        val informationTermsHtmlFilename = getString(R.string.information_technical_html_path)
        binding.informationTechnicalWebview.loadUrl(AssetConstants.ANDROID_ASSET_PATH + informationTermsHtmlFilename)
    }
}
