package de.rki.coronawarnapp.test.eventregistration.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.FragmentTestCreateeventBinding
import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass
import de.rki.coronawarnapp.server.protocols.internal.evreg.SignedEventOuterClass
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

class CreateEventTestFragment : Fragment(R.layout.fragment_test_createevent) {

    private val binding: FragmentTestCreateeventBinding by viewBindingLazy()

    private val eventString = "Event"
    private val locationString = "Location"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSpinner()
        initOnCreateEventClicked()
    }

    private fun initOnCreateEventClicked() = with(binding) {
        createEventButton.setOnClickListener {
            try {
                val description = eventDescription.text.toString()
                val location = eventLocation.text.toString()
                val start = eventStartEditText.text.toString()
                val end = eventEndEditText.text.toString()
                val defaultCheckInLengthInMinutes = eventDefaultCheckinLengthInMinutes.text.toString()

                val startDate =
                    if (start.isBlank()) null else DateTime.parse(start, DateTimeFormat.forPattern("YYYY-MM-dd HH:mm"))
                val endDate =
                    if (end.isBlank()) null else DateTime.parse(end, DateTimeFormat.forPattern("YYYY-MM-dd HH:mm"))

                val startTimeStamp = startDate?.toInstant()?.millis?.toInt() ?: 0
                val endTimeStamp = endDate?.toInstant()?.millis?.toInt() ?: 0

                // details yet tbd, but we basically sent our event entity to the backend ...
                val event = EventOuterClass.Event.newBuilder()
                    .setDescription(description)
                    // .setLocation(location) // will probably added in a future protobuf
                    .setStart(startTimeStamp)
                    .setEnd(endTimeStamp)
                    .setDefaultCheckInLengthInMinutes(defaultCheckInLengthInMinutes.toInt())

                // and the server responds with an event object with additional information
                val serverEvent = event
                    // .setGuid() // Server creates GUID
                    .build()

                val signedEvent = SignedEventOuterClass.SignedEvent.newBuilder()
                    .setEvent(serverEvent)
                    .setSignature(ByteString.copyFrom("ServerSignature".toByteArray()))
                    .build()

                resultText.text = "Successfully entered envent: $signedEvent"
            } catch (e: Exception) {
                Timber.d("Invalid Input Values: $e")
                resultText.text = "There is something wrong with your input values, please check again."
            } finally {
                it.hideKeyboard()
            }
        }
    }

    private fun initSpinner() {
        val items = listOf(eventString, locationString)
        with(binding.eventOrLocationSpinner.editText as AutoCompleteTextView) {
            setText(items.first(), false)
            setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items))
            doAfterTextChanged { }
            doOnTextChanged { text, start, before, count ->
                Timber.d("text: $text, start: $start, before: $before, count: $count")

                when (text.toString()) {
                    eventString -> {
                        binding.eventStart.visibility = View.VISIBLE
                        binding.eventEnd.visibility = View.VISIBLE
                    }
                    locationString -> {
                        binding.eventStart.visibility = View.GONE
                        binding.eventEnd.visibility = View.GONE
                    }
                }
            }
        }
    }
}
