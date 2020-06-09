package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationPrivacyBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.AssetConstants
import kotlinx.android.synthetic.main.fragment_onboarding_privacy.onboarding_privacy_webview

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
    }

    private fun setButtonOnClickListener() {
        binding.informationPrivacyHeader.headerToolbar.setNavigationOnClickListener {
            (activity as MainActivity).goBack()
        }
    }

    private fun loadInformationPrivacyWebView() {
        val informationPrivacyHtmlFilename = getString(R.string.information_privacy_html_path)
        onboarding_privacy_webview.loadUrl(AssetConstants.ANDROID_ASSET_PATH + informationPrivacyHtmlFilename)
    }
}
