package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationTermsBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.AssetConstants
import kotlinx.android.synthetic.main.fragment_onboarding_privacy.*

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
    }

    private fun setButtonOnClickListener() {
        binding.informationTermsHeader.headerToolbar.setNavigationOnClickListener {
            (activity as MainActivity).goBack()
        }
    }

    private fun loadInformationPrivacyWebView() {
        val informationTermsHtmlFilename = getString(R.string.information_terms_body)
        onboarding_privacy_webview.loadUrl(AssetConstants.ANDROID_ASSET_PATH + informationTermsHtmlFilename)
    }
}
