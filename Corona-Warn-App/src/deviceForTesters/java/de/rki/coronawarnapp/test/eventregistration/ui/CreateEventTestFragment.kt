package de.rki.coronawarnapp.test.eventregistration.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestCreateeventBinding
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import timber.log.Timber

class CreateEventTestFragment : Fragment(R.layout.fragment_test_createevent) {

    private val binding: FragmentTestCreateeventBinding by viewBindingLazy()

    private val eventString = "Event"
    private val locationString = "Location"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSpinner()
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
