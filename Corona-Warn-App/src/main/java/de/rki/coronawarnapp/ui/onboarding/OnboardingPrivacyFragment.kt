package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingPrivacyBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.util.AssetConstants

/**
 * This fragment informs the user regarding privacy.
 */
class OnboardingPrivacyFragment : BaseFragment() {
    companion object {
        private val TAG: String? = OnboardingPrivacyFragment::class.simpleName
    }

    private var _binding: FragmentOnboardingPrivacyBinding? = null
    private val binding: FragmentOnboardingPrivacyBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingPrivacyBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
        loadInformationPrivacyWebView()
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonNext.setOnClickListener {
            doNavigate(
                OnboardingPrivacyFragmentDirections.actionOnboardingPrivacyFragmentToOnboardingTracingFragment()
            )
        }
        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            (activity as OnboardingActivity).goBack()
        }
    }

    private fun loadInformationPrivacyWebView() {
        val informationPrivacyHtmlFilename = getString(R.string.information_privacy_html_path)
        binding.onboardingPrivacyWebview.loadUrl(AssetConstants.ANDROID_ASSET_PATH + informationPrivacyHtmlFilename)
    }
}
