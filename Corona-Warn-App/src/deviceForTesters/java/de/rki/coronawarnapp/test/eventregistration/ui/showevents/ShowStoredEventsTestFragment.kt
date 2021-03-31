package de.rki.coronawarnapp.test.eventregistration.ui.showevents

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestShowstoredeventsBinding
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
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

        binding.deleteAllEvents.setOnClickListener {
            vm.deleteAllEvents()
        }
    }

    private fun TraceLocation.getSimpleUIString(): String {
        return listOf(
            "id = $id",
            "type = $type",
            "description = $description",
            "location = $address",
            "startTime = $startDate",
            "endTime = $endDate",
            "defaultCheckInLengthInMinutes = $defaultCheckInLengthInMinutes",
            "cryptographicSeed = $cryptographicSeed",
            "cnPublicKey = $cnPublicKey"
        ).joinToString(separator = "\n")
    }
}
