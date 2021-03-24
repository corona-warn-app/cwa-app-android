package de.rki.coronawarnapp.test.eventregistration.ui.createevent

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.FragmentTestCreateeventBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class CreateEventTestFragment : Fragment(R.layout.fragment_test_createevent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: CreateEventTestViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestCreateeventBinding by viewBindingLazy()

    private val eventString = "Event"
    private val locationString = "Location"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSpinner()
        initOnCreateEventClicked()
        observeViewModelResult()
    }

    private fun observeViewModelResult() {
        vm.result.observe2(this) {
            when (it) {
                is CreateEventTestViewModel.Result.Success ->
                    binding.resultText.text = "Successfully stored: ${it.eventEntity}"
                is CreateEventTestViewModel.Result.Error ->
                    binding.resultText.text = "There is something wrong with your input values, please check again."
            }
        }
    }

    private fun initOnCreateEventClicked() = with(binding) {
        createEventButton.setOnClickListener {
            createEvent()
            it.hideKeyboard()
        }
        sendToServerButton.setOnClickListener {
            createEvent(sendToServer = true)
            it.hideKeyboard()
        }
    }

    private fun FragmentTestCreateeventBinding.createEvent(sendToServer: Boolean = false) {
        vm.createEvent(
            eventOrLocationSpinner.editText!!.text.toString(),
            eventDescription.text.toString(),
            eventAddress.text.toString(),
            eventStartEditText.text.toString(),
            eventEndEditText.text.toString(),
            eventDefaultCheckinLengthInMinutes.text.toString(),
            sendToServer
        )
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
                        binding.eventStartEditText.text = null
                        binding.eventEndEditText.text = null
                    }
                }
            }
        }
    }
}
