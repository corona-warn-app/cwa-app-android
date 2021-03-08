package de.rki.coronawarnapp.test.eventregistration.ui.showevents

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestShowstoredeventsBinding
import de.rki.coronawarnapp.eventregistration.events.HostedEvent
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ShowStoredEventsTestFragment : Fragment(R.layout.fragment_test_showstoredevents), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: ShowStoredEventsTestViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestShowstoredeventsBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.storedEvents.observe2(this) { events ->
            binding.storedEvents.text = events.joinToString(separator = "\n\n") { it.getSimpleUIString() }
        }
    }

    private fun HostedEvent.getSimpleUIString(): String {
        return listOf(
            "guid = $guid",
            "description = $description",
            "location = $location",
            "startTime = $startTime",
            "endTime = $endTime",
            "defaultCheckInLengthInMinutes = $defaultCheckInLengthInMinutes",
            "signature = $signature"
        ).joinToString(separator = "\n")
    }
}
