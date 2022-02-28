package de.rki.coronawarnapp.dccreissuance.ui.consent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class DccReissuanceConsentFragment : Fragment(R.layout.fragment_dcc_reissuance_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val args by navArgs<DccReissuanceConsentFragmentArgs>()
    private val viewModel: DccReissuanceConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccReissuanceConsentViewModel.Factory
            factory.create(
                personIdentifierCode = args.personIdentifierCode,
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.dccReissuanceData.observe(viewLifecycleOwner) {
            // TO-DO
        }
    }
}
