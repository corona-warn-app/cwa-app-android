package de.rki.coronawarnapp.contactdiary.ui.sheets.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.focusAndShowKeyboard
import de.rki.coronawarnapp.databinding.ContactDiaryLocationBottomSheetFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryLocationBottomSheetDialogFragment : BottomSheetDialogFragment(), AutoInject {
    private var _binding: ContactDiaryLocationBottomSheetFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryLocationBottomSheetDialogViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryLocationBottomSheetDialogViewModel.Factory
            factory.create(navArgs.addedAt)
        }
    )

    private val navArgs: ContactDiaryLocationBottomSheetDialogFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ContactDiaryLocationBottomSheetFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val location = navArgs.selectedLocation
        if (location != null) {
            binding.contactDiaryLocationBottomSheetTextInputEditText.setText(location.locationName)
            binding.contactDiaryLocationBottomSheetDeleteButton.visibility = View.VISIBLE
            binding.contactDiaryLocationBottomSheetDeleteButton.setOnClickListener {
                DialogHelper.showDialog(deleteLocationConfirmationDialog)
            }
            binding.contactDiaryLocationBottomSheetSaveButton.setOnClickListener {
                viewModel.updateLocation(location)
            }
        } else {
            binding.contactDiaryLocationBottomSheetDeleteButton.visibility = View.GONE
            binding.contactDiaryLocationBottomSheetSaveButton.setOnClickListener {
                viewModel.addLocation()
            }
        }

        binding.contactDiaryLocationBottomSheetCloseButton.setOnClickListener {
            viewModel.closePressed()
        }

        binding.contactDiaryLocationBottomSheetTextInputEditText.doAfterTextChanged {
            viewModel.textChanged(it.toString())
        }

        binding.contactDiaryLocationBottomSheetTextInputEditText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    if (viewModel.isValid.value == true) {
                        binding.contactDiaryLocationBottomSheetSaveButton.performClick()
                    }
                    false
                }
                else -> true
            }
        }

        binding.contactDiaryLocationBottomSheetTextInputEditText.focusAndShowKeyboard()

        viewModel.shouldClose.observe2(this) {
            dismiss()
        }

        viewModel.isValid.observe2(this) {
            binding.contactDiaryLocationBottomSheetTextInputLayout.isErrorEnabled = it
            binding.contactDiaryLocationBottomSheetSaveButton.isEnabled = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val deleteLocationConfirmationDialog by lazy {
        DialogHelper.DialogInstance(
            requireActivity(),
            R.string.contact_diary_delete_location_title,
            R.string.contact_diary_delete_locations_message,
            R.string.contact_diary_delete_button_positive,
            R.string.contact_diary_delete_button_negative,
            positiveButtonFunction = {
                navArgs.selectedLocation?.let {
                    viewModel.deleteLocation(it)
                }
            }
        )
    }
}
