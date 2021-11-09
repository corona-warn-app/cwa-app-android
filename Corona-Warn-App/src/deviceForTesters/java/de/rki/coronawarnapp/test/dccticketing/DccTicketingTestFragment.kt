package de.rki.coronawarnapp.test.dccticketing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestDccTicketingBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DccTicketingTestFragment : Fragment(R.layout.fragment_test_dcc_ticketing), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: DccTicketingTestViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentTestDccTicketingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "DCC Ticketing",
            description = "Dcc Ticketing validation",
            targetId = R.id.dccTicketingConsentOneFragment
        )
    }
}
