package de.rki.coronawarnapp.covidcertificate.ui.info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateInfoFragmentBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class CovidCertificateInfoFragment : Fragment(R.layout.covid_certificate_info_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: CovidCertificateInfoViewModel by cwaViewModels { viewModelFactory }
    private val binding: CovidCertificateInfoFragmentBinding by viewBinding()
    private val args by navArgs<CovidCertificateInfoFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            if (!args.showBottomNav) {
                toolbar.apply {
                    navigationIcon = context.getDrawableCompat(R.drawable.ic_close)
                    navigationContentDescription = getString(R.string.accessibility_close)
                }
            }
            toolbar.setNavigationOnClickListener { popBackStack() }
            privacyInformation.setOnClickListener { viewModel.onDataPrivacyClick() }
            buttonContinue.setOnClickListener { viewModel.onContinueClick() }
        }

        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                CovidCertificateInfoViewModel.Event.NavigateToDataPrivacy -> doNavigate(
                    CovidCertificateInfoFragmentDirections.actionCovidCertificateInfoFragmentToPrivacyFragment()
                )
                CovidCertificateInfoViewModel.Event.NavigateToPersonOverview -> doNavigate(
                    CovidCertificateInfoFragmentDirections.actionCovidCertificateInfoFragmentToPersonOverviewFragment()
                )
            }
        }
    }
}
