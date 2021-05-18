package de.rki.coronawarnapp.vaccination.ui.consent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationConsentFragmentBinding
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
                    doNavigate(VaccinationConsentFragmentDirections.vaccinationConsentFragmentToQrCodeFragment())
                }
            }
        }
    }
}
