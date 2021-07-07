package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.failed

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationFailedFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class DccValidationFailedFragment : Fragment(R.layout.covid_certificate_validation_failed_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding by viewBinding<CovidCertificateValidationFailedFragmentBinding>()
    private val args by navArgs<DccValidationFailedFragmentArgs>()
    private val viewModel: DccValidationFailedViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccValidationFailedViewModel.Factory
            factory.create(
                validation = args.validation
            )
        }
    )
}
