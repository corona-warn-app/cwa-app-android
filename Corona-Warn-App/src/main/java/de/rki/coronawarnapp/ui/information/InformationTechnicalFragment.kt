package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationTechnicalBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.AssetConstants
import kotlinx.android.synthetic.main.fragment_onboarding_privacy.*

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
    }

    private fun setButtonOnClickListener() {
        binding.informationTechnicalHeader.headerToolbar.setNavigationOnClickListener {
            (activity as MainActivity).goBack()
        }
    }

    private fun loadInformationPrivacyWebView() {
        val informationTechnicalHtmlFilename = getString(R.string.information_technical_body)
        onboarding_privacy_webview.loadUrl(AssetConstants.ANDROID_ASSET_PATH + informationTechnicalHtmlFilename)
    }
}
