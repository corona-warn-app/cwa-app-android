package de.rki.coronawarnapp.ui.eventregistration.attendee.confirm

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentConfirmCheckInBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ConfirmCheckInFragment : Fragment(R.layout.fragment_confirm_check_in), AutoInject {

    private val navArgs by navArgs<ConfirmCheckInFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: ConfirmCheckInViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, savedState ->
            factory as ConfirmCheckInViewModel.Factory
            factory.create(navArgs.verifiedTraceLocation)
        }
    )
    private val binding: FragmentConfirmCheckInBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { viewModel.onClose() }
        }

        viewModel.events.observe2(this) { navEvent ->
            when (navEvent) {
                ConfirmCheckInNavigation.BackNavigation -> popBackStack()
                ConfirmCheckInNavigation.ConfirmNavigation -> {
                    // TODO Navigate to the rightful destination
                    popBackStack()
                }
            }
        }
    }
}
