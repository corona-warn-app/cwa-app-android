package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Patterns
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfile
import de.rki.coronawarnapp.databinding.RatProfileCreateFragmentBinding
import de.rki.coronawarnapp.ui.coronatest.rat.profile.create.RATProfileCreateFragmentViewModel.Companion.format
import de.rki.coronawarnapp.ui.view.addEmojiFilter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.joda.time.LocalDate
import javax.inject.Inject

class RATProfileCreateFragment : Fragment(R.layout.rat_profile_create_fragment), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val binding: RatProfileCreateFragmentBinding by viewBinding()
    private val viewModel: RATProfileCreateFragmentViewModel by cwaViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener {
                viewModel.navigateBack()
            }

            profileSaveButton.setOnClickListener {
                it.hideKeyboard()
                viewModel.createProfile()
            }

            // Full name
            firstNameInputEdit.addEmojiFilter().doAfterTextChanged { viewModel.firstNameChanged(it.toString()) }
            lastNameInputEdit.addEmojiFilter().doAfterTextChanged { viewModel.lastNameChanged(it.toString()) }

            // Birth date
            birthDateInputEdit.setOnClickListener { openDatePicker() }
            birthDateInputEdit.doAfterTextChanged {
                val dob = if (it.toString().isBlank()) null else it.toString()
                viewModel.birthDateChanged(dob)
            }

            // Address
            streetInputEdit.addEmojiFilter().doAfterTextChanged { viewModel.streetChanged(it.toString()) }
            cityInputEdit.addEmojiFilter().doAfterTextChanged { viewModel.cityChanged(it.toString()) }
            zipCodeInputEdit.doAfterTextChanged { viewModel.zipCodeChanged(it.toString()) }

            // Phone
            phoneInputEdit.doAfterTextChanged {
                // Propagate phone number to view model if it matches the pattern
                if (Patterns.PHONE.matcher(it.toString()).matches()) {
                    viewModel.phoneChanged(it.toString())
                } else {
                    viewModel.phoneChanged("")
                }
            }
            phoneInputEdit.setOnFocusChangeListener { _, hasFocus ->
                // Validate phone number
                if (!hasFocus && !Patterns.PHONE.matcher(phoneInputEdit.text.toString()).matches()) {
                    phoneInputLayout.error = getRoot().getResources().getString(R.string.rat_profile_create_phone_error)
                } else {
                    phoneInputLayout.error = null
                }
            }
            phoneInputEdit.addTextChangedListener(PhoneNumberFormattingTextWatcher())

            // E-mail
            emailInputEdit.addEmojiFilter().doAfterTextChanged {
                // Propagate email to view model if it matches the pattern
                if (Patterns.EMAIL_ADDRESS.matcher(it.toString()).matches()) {
                    viewModel.emailChanged(it.toString())
                } else {
                    viewModel.emailChanged("")
                }
            }
            emailInputEdit.setOnFocusChangeListener { _, hasFocus ->
                // Validate email
                if (!hasFocus && !Patterns.EMAIL_ADDRESS.matcher(emailInputEdit.text.toString()).matches()) {
                    emailInputLayout.error = getRoot().getResources().getString(R.string.rat_profile_create_email_error)
                } else {
                    emailInputLayout.error = null
                }
            }

            viewModel.profile.observe(viewLifecycleOwner) { profileSaveButton.isEnabled = it.isValid }
            viewModel.latestProfile.observe(viewLifecycleOwner) { it?.let { bindProfile(it) } }
            viewModel.events.observe(viewLifecycleOwner) {
                when (it) {
                    CreateRATProfileNavigation.Back -> popBackStack()
                    CreateRATProfileNavigation.ProfileScreen -> doNavigate(
                        RATProfileCreateFragmentDirections
                            .actionRatProfileCreateFragmentToRatProfileQrCodeFragment()
                    )
                }
            }
        }

    private fun RatProfileCreateFragmentBinding.bindProfile(data: RATProfile) {
        firstNameInputEdit.setText(data.firstName)
        lastNameInputEdit.setText(data.lastName)

        data.birthDate?.let { birthDateInputEdit.setText(it.toDayFormat()) }

        zipCodeInputEdit.setText(data.zipCode)
        streetInputEdit.setText(data.street)
        cityInputEdit.setText(data.city)

        phoneInputEdit.setText(data.phone)
        emailInputEdit.setText(data.email)
    }

    private fun openDatePicker() {
        // Only allow date selections in the past
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())

        MaterialDatePicker.Builder
            .datePicker()
            .setCalendarConstraints(constraintsBuilder.build())
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    binding.birthDateInputEdit.setText(LocalDate(it).toString(format))
                }
            }
            .show(childFragmentManager, "RATProfileCreateFragment.MaterialDatePicker")
    }
}
