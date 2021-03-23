package de.rki.coronawarnapp.ui.eventregistration.attendee.confirm

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
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
            factory.create(navArgs.verifiedTraceLocation, navArgs.editCheckInId)
        }
    )
    private val binding: FragmentConfirmCheckInBinding by viewBindingLazy()
    private val args by navArgs<ConfirmCheckInFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { viewModel.onClose() }
            confirmButton.setOnClickListener { viewModel.onConfirmTraceLocation() }
            // TODO bind final UI
            args.verifiedTraceLocation?.let {
                val traceLocation = it.traceLocation
                eventGuid.text = "GUID: %s".format(traceLocation.guid)
                startTime.text = "Start time: %s".format(traceLocation.startDate)
                endTime.text = "End time: %s".format(traceLocation.endDate)
                description.text = "Description: %s".format(traceLocation.description)
            }

            if (navArgs.editCheckInId != 0L) {
                Toast.makeText(requireContext(), "EDIT CHECKIN MODE", Toast.LENGTH_SHORT).show()
            }
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
