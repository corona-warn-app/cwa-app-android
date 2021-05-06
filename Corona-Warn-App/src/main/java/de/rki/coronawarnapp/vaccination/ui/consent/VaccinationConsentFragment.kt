package de.rki.coronawarnapp.vaccination.ui.consent

import android.os.Bundle
import android.text.SpannedString
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationConsentFragmentBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class VaccinationConsentFragment : Fragment(R.layout.vaccination_consent_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: VaccinationConsentViewModel by cwaViewModels { viewModelFactory }

    private val binding: VaccinationConsentFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            vaccinationConsentInfoSubtitle.text = getSpannedInfo()
            vaccinationConsentPrivacyInformation.setOnClickListener {
                viewModel.onDataPrivacyClick()
            }
            buttonConsent.setOnClickListener {
                viewModel.onConsentClick()
            }
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                VaccinationConsentNavigationEvent.NavigateToDataPrivacy -> {
                    doNavigate(VaccinationConsentFragmentDirections.vaccinationConsentFragmentToPrivacyFragment())
                }
                VaccinationConsentNavigationEvent.NavigateToQrCodeScan -> {
                    // TODO: implement in when screen is ready
                }
            }
        }
    }

    private fun getSpannedInfo(): SpannedString = buildSpannedString {
        append(getString(R.string.vaccination_consent_info_subtitle_text_part1))
        color(requireContext().getColorCompat(R.color.colorAccent)) {
            append(getString(R.string.vaccination_consent_info_subtitle_text_part2))
        }
    }
}
