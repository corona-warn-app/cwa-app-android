package de.rki.coronawarnapp.dccreissuance.ui.success

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DccReissuanceSuccessFragment : Fragment(R.layout.fragment_dcc_reissuance_success) {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: DccReissuanceSuccessViewModel by cwaViewModels { viewModelFactory }
}
