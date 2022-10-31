package de.rki.coronawarnapp.contactdiary.ui.location

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.focusAndShowKeyboard
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.ContactDiaryAddLocationFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setTextOnTextInput
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryAddLocationFragment : Fragment(R.layout.contact_diary_add_location_fragment), AutoInject {

    private val binding: ContactDiaryAddLocationFragmentBinding by viewBinding()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryAddLocationViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryAddLocationViewModel.Factory
            factory.create(navArgs.addedAt)
        }
    )

    private val navArgs: ContactDiaryAddLocationFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transform = MaterialContainerTransform()
        sharedElementEnterTransition = transform
        sharedElementReturnTransition = transform
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val location = navArgs.selectedLocation
        if (location != null) {
            binding.apply {
                locationNameInputEdit.setText(location.locationName)
                locationPhoneInput.setTextOnTextInput(location.phoneNumber, endIconVisible = false)
                locationEmailInput.setTextOnTextInput(location.emailAddress, endIconVisible = false)
                locationDeleteButton.visibility = View.VISIBLE
                locationDeleteButton.setOnClickListener { deleteLocationConfirmationDialog() }
                locationSaveButton.setOnClickListener {
                    viewModel.updateLocation(
                        location,
                        phoneNumber = binding.locationPhoneInput.text.toString(),
                        emailAddress = binding.locationEmailInput.text.toString()
                    )
                }
            }
            viewModel.locationChanged(location.locationName)
        } else {
            binding.apply {
                locationDeleteButton.visibility = View.GONE
                locationSaveButton.setOnClickListener {
                    viewModel.addLocation(
                        phoneNumber = binding.locationPhoneInput.text.toString(),
                        emailAddress = binding.locationEmailInput.text.toString()
                    )
                }
            }
        }

        binding.apply {
            locationNameInputEdit.focusAndShowKeyboard()

            locationCloseButton.setOnClickListener {
                viewModel.closePressed()
            }
            locationNameInputEdit.doAfterTextChanged {
                viewModel.locationChanged(it.toString())
            }

            locationEmailInput.setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        if (viewModel.isValid.value == true) {
                            binding.locationSaveButton.performClick()
                        }
                        false
                    }
                    else -> true
                }
            }
        }

        viewModel.shouldClose.observe2(this) {
            binding.root.hideKeyboard()
            popBackStack()
        }

        viewModel.isValid.observe2(this) {
            binding.locationSaveButton.isEnabled = it
        }
    }

    private fun deleteLocationConfirmationDialog() = displayDialog {
        title(R.string.contact_diary_delete_location_title)
        message(R.string.contact_diary_delete_location_message)
        positiveButton(R.string.contact_diary_delete_location_message) {
            navArgs.selectedLocation?.let {
                viewModel.deleteLocation(it)
            }
        }
        negativeButton(R.string.contact_diary_delete_button_negative)
        setDeleteDialog(true)
    }
}
