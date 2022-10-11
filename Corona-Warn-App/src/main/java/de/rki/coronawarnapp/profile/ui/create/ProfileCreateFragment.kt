package de.rki.coronawarnapp.profile.ui.create

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Patterns
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.ProfileCreateFragmentBinding
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.ui.view.addEmojiFilter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.toLocalDateUserTz
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

class ProfileCreateFragment : Fragment(R.layout.profile_create_fragment), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val navArgs by navArgs<ProfileCreateFragmentArgs>()
    private val binding: ProfileCreateFragmentBinding by viewBinding()
    private val viewModel: ProfileCreateFragmentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ProfileCreateFragmentViewModel.Factory
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
            phoneInputEdit.doOnTextChanged { text, _, _, _ ->
                if (Patterns.PHONE.matcher(text.toString()).matches() || text.toString().isBlank()) {
                    phoneInputLayout.error = null
                    viewModel.phoneChanged(text.toString())
                } else {
                    phoneInputLayout.error = root.resources.getString(R.string.rat_profile_create_phone_error)
                    profileSaveButton.isEnabled = false
                }
            }
            phoneInputEdit.addTextChangedListener(PhoneNumberFormattingTextWatcher())

            emailInputEdit.addEmojiFilter().doOnTextChanged { text, _, _, _ ->
                if (Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches() || text.toString().isBlank()) {
                    emailInputLayout.error = null
                    viewModel.emailChanged(text.toString())
                } else {
                    emailInputLayout.error = root.resources.getString(R.string.rat_profile_create_email_error)
                    profileSaveButton.isEnabled = false
                }
            }

            viewModel.profile.observe(viewLifecycleOwner) { profileSaveButton.isEnabled = it.isValid }
            viewModel.savedProfile.observe(viewLifecycleOwner) { it?.let { bindProfile(it) } }
            viewModel.events.observe(viewLifecycleOwner) {
                when (it) {
                    CreateProfileNavigation.Back -> popBackStack()
                    is CreateProfileNavigation.ProfileScreen -> findNavController().navigate(
                        ProfileCreateFragmentDirections
                            .actionProfileCreateFragmentToProfileQrCodeFragment(it.profileId)
                    )
                }
            }
        }

    private fun ProfileCreateFragmentBinding.bindProfile(data: Profile) {
        firstNameInputEdit.setText(data.firstName)
        lastNameInputEdit.setText(data.lastName)

        data.birthDate?.let {
            birthDateInputEdit.setText(it.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
        }

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
                    binding.birthDateInputEdit.setText(Instant.ofEpochMilli(it).toLocalDateUserTz().format(formatter))
                }
            }
            .show(childFragmentManager, "ProfileCreateFragment.MaterialDatePicker")
    }
}
