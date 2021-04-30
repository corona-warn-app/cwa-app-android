package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.RatProfileCreateFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.joda.time.LocalDate
import javax.inject.Inject

class RATProfileCreateFragment : Fragment(R.layout.rat_profile_create_fragment), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val binding: RatProfileCreateFragmentBinding by viewBindingLazy()
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
            firstNameInputEdit.doAfterTextChanged { viewModel.firstNameChanged(it.toString()) }
            lastNameInputEdit.doAfterTextChanged { viewModel.lastNameChanged(it.toString()) }

            // Birth date
            birthDateInputEdit.setOnClickListener { openDatePicker() }

            // Address
            streetInputEdit.doAfterTextChanged { viewModel.streetChanged(it.toString()) }
            zipCodeInputEdit.doAfterTextChanged { viewModel.zipCodeChanged(it.toString()) }
            cityInputEdit.doAfterTextChanged { viewModel.cityChanged(it.toString()) }

            // Phone
            phoneInputEdit.doAfterTextChanged { viewModel.phoneChanged(it.toString()) }

            // E-mail
            emailInputEdit.doAfterTextChanged { viewModel.emailChanged(it.toString()) }

            viewModel.profile.observe(viewLifecycleOwner) {
                profileSaveButton.isEnabled = it?.isValid == true
            }

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

    private fun openDatePicker() {
        MaterialDatePicker.Builder
            .datePicker()
            .build()
            .apply {
                addOnPositiveButtonClickListener { timestamp ->
                    val localDate = LocalDate(timestamp)
                    binding.birthDateInputEdit.setText(
                        localDate.toString("dd.MM.yyyy")
                    )
                    viewModel.birthDateChanged(localDate)
                }
            }
            .show(childFragmentManager, "RATProfileCreateFragment.MaterialDatePicker")
    }
}
