package de.rki.coronawarnapp.ui.presencetracing.attendee.confirm

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentConfirmCheckInBinding
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
import de.rki.coronawarnapp.ui.durationpicker.DurationPicker
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import java.time.Duration
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class ConfirmCheckInFragment : Fragment(R.layout.fragment_confirm_check_in) {

    @Inject lateinit var factory: ConfirmCheckInViewModel.Factory

    private val navArgs by navArgs<ConfirmCheckInFragmentArgs>()
    private val locationViewModel by navGraphViewModels<QrcodeSharedViewModel>(R.id.nav_graph)
    private val viewModel: ConfirmCheckInViewModel by assistedViewModel {
        factory.create(
            verifiedTraceLocationId = navArgs.locationId,
            qrcodeSharedViewModel = locationViewModel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    private val binding: FragmentConfirmCheckInBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                title.alpha = (
                    1.0f - abs(verticalOffset / (appBarLayout.totalScrollRange.toFloat() * 0.6f))
                    )
            }

            toolbar.setNavigationOnClickListener { viewModel.onClose() }

            confirmCheckinSettingsCardCheckoutToggle.setOnCheckedChangeListener { _, isChecked ->
                viewModel.createJournalEntryToggled(isChecked)
            }

            confirmCheckinSettingsCardCheckoutTimeRow.setOnClickListener {
                viewModel.dateSelectorClicked()
            }

            confirmCheckinConfirmButton.setOnClickListener {
                viewModel.onConfirmTraceLocation()
            }
        }

        viewModel.events.observe(viewLifecycleOwner) { navEvent ->
            when (navEvent) {
                ConfirmCheckInNavigation.BackNavigation -> popBackStack()
                ConfirmCheckInNavigation.ConfirmNavigation -> popBackStack()
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            with(binding) {
                confirmCheckinInfoCardHeader.text = getString(uiState.typeRes)
                confirmCheckinInfoCardTitle.text = uiState.description
                confirmCheckinInfoCardAddress.text = uiState.address
                confirmCheckinSettingsCardCheckoutToggle.isChecked = uiState.createJournalEntry
                confirmCheckinSettingsCardCheckoutTime.text = uiState.checkInEnd

                confirmCheckinEventInPastCard.isGone = !uiState.eventInPastVisible

                confirmCheckinEventInFutureCard.isGone = !uiState.eventInFutureVisible
                confirmCheckinEventInFutureCardText.text = getString(
                    R.string.confirm_checkin_event_in_future_card_text,
                    uiState.eventInFutureDateText,
                    uiState.eventInFutureTimeText
                )
                confirmCheckinConfirmButton.isEnabled = uiState.confirmButtonEnabled
            }
        }

        viewModel.openDatePickerEvent.observe(viewLifecycleOwner) { time ->
            showDurationPicker(time) {
                viewModel.durationUpdated(it)
            }
        }
    }

    private fun showDurationPicker(
        defaultValue: String?,
        callback: (Duration) -> Unit
    ) {
        val durationPicker = DurationPicker.Builder()
            .duration(defaultValue ?: "00:00")
            .title(getString(R.string.duration_dialog_title))
            .build()
        durationPicker.show(parentFragmentManager, DURATION_PICKER_TAG)
        durationPicker.setDurationChangeListener(callback)
    }

    companion object {
        private const val DURATION_PICKER_TAG = "duration_picker"
        fun uri(locationId: String) = "cwa://check-in/?locationId=$locationId".toUri()
    }
}
