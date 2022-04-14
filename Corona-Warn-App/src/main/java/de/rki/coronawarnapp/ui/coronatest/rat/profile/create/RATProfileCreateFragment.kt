package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Patterns
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.RatProfileCreateFragmentBinding
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.ui.view.addEmojiFilter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import javax.inject.Inject

class RATProfileCreateFragment : Fragment(R.layout.rat_profile_create_fragment), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val formatter: DateTimeFormatter = DateTimeFormat.mediumDate()
    private val navArgs by navArgs<RATProfileCreateFragmentArgs>()
    private val binding: RatProfileCreateFragmentBinding by viewBinding()
    private val viewModel: RATProfileCreateFragmentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as RATProfileCreateFragmentViewModel.Factory
            factory.create(
                formatter,
                if (navArgs.profileId > 0) navArgs.profileId else null
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transform = MaterialContainerTransform()
        sharedElementEnterTransition = transform
        sharedElementReturnTransition = transform
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            toolbar.setNavigationOnClickListener {
                viewModel.navigateBack()
            }

            profileSaveButton.setOnClickListener {
                it.hideKeyboard()
                viewModel.saveProfile()
            }

            // Full name
            firstNameInputEdit.addEmojiFilter().doAfterTextChanged { viewModel.firstNameChanged(it.toString()) }
            lastNameInputEdit.addEmojiFilter().doAfterTextChanged { viewModel.lastNameChanged(it.toString()) }

            // Birth date
            birthDateInputEdit.setOnClickListener { openDatePicker() }
            birthDateInputEdit.doAfterTextChanged {
                val dob = it.toString().ifBlank { null }
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
                    phoneInputLayout.error = root.resources.getString(R.string.rat_profile_create_phone_error)
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
                    emailInputLayout.error = root.resources.getString(R.string.rat_profile_create_email_error)
                } else {
                    emailInputLayout.error = null
                }
            }

            viewModel.profile.observe(viewLifecycleOwner) { profileSaveButton.isEnabled = it.isValid }
            viewModel.savedProfile.observe(viewLifecycleOwner) { it?.let { bindProfile(it) } }
            viewModel.events.observe(viewLifecycleOwner) {
                when (it) {
                    CreateRATProfileNavigation.Back -> popBackStack()
                    is CreateRATProfileNavigation.ProfileScreen -> doNavigate(
                        RATProfileCreateFragmentDirections
                            .actionRatProfileCreateFragmentToRatProfileQrCodeFragment(it.id)
                    )
                }
            }
        }

    private fun RatProfileCreateFragmentBinding.bindProfile(data: Profile) {
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
            .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
            .setTitleText(getString(R.string.rat_profile_create_birth_date_hint))
            .build()
            .apply {
                addOnPositiveButtonClickListener {
                    binding.birthDateInputEdit.setText(LocalDate(it).toString(formatter))
                }
            }
            .show(childFragmentManager, "RATProfileCreateFragment.MaterialDatePicker")
    }
}
