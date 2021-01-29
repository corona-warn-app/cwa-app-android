package de.rki.coronawarnapp.contactdiary.ui.sheets.person

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.focusAndShowKeyboard
import de.rki.coronawarnapp.databinding.ContactDiaryPersonBottomSheetFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryPersonBottomSheetDialogFragment : BottomSheetDialogFragment(), AutoInject {
    private var _binding: ContactDiaryPersonBottomSheetFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryPersonBottomSheetDialogViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryPersonBottomSheetDialogViewModel.Factory
            factory.create(navArgs.addedAt)
        }
    )

    private val navArgs: ContactDiaryPersonBottomSheetDialogFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ContactDiaryPersonBottomSheetFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val person = navArgs.selectedPerson
        if (person != null) {
            binding.contactDiaryPersonBottomSheetTextInputEditText.setText(person.fullName)
            binding.contactDiaryPersonBottomSheetDeleteButton.visibility = View.VISIBLE
            binding.contactDiaryPersonBottomSheetDeleteButton.setOnClickListener {
                DialogHelper.showDialog(deletePersonConfirmationDialog)
            }
            binding.contactDiaryPersonBottomSheetSaveButton.setOnClickListener {
                viewModel.updatePerson(person)
            }
        } else {
            binding.contactDiaryPersonBottomSheetDeleteButton.visibility = View.GONE
            binding.contactDiaryPersonBottomSheetSaveButton.setOnClickListener {
                viewModel.addPerson()
            }
        }

        binding.contactDiaryPersonBottomSheetCloseButton.setOnClickListener {
            viewModel.closePressed()
        }

        binding.contactDiaryPersonBottomSheetTextInputEditText.doAfterTextChanged {
            viewModel.textChanged(it.toString())
        }

        binding.contactDiaryPersonBottomSheetTextInputEditText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                IME_ACTION_DONE -> {
                    if (viewModel.isValid.value == true) {
                        binding.contactDiaryPersonBottomSheetSaveButton.performClick()
                    }
                    false
                }
                else -> true
            }
        }

        binding.contactDiaryPersonBottomSheetTextInputEditText.focusAndShowKeyboard()

        viewModel.shouldClose.observe2(this) {
            dismiss()
        }

        viewModel.isValid.observe2(this) { isValid ->
            binding.contactDiaryPersonBottomSheetTextInputLayout.isErrorEnabled = isValid
            binding.contactDiaryPersonBottomSheetSaveButton.isEnabled = isValid
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val deletePersonConfirmationDialog by lazy {
        DialogHelper.DialogInstance(
            requireActivity(),
            R.string.contact_diary_delete_person_title,
            R.string.contact_diary_delete_persons_message,
            R.string.contact_diary_delete_button_positive,
            R.string.contact_diary_delete_button_negative,
            positiveButtonFunction = {
                navArgs.selectedPerson?.let {
                    viewModel.deletePerson(it)
                }
            }
        )
    }
}
