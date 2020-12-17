package de.rki.coronawarnapp.contactdiary.ui.day.sheets.person

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryPersonBottomSheetFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ContactDiaryPersonBottomSheetDialogFragment : BottomSheetDialogFragment(), AutoInject {
    private var _binding: ContactDiaryPersonBottomSheetFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryPersonBottomSheetDialogViewModel by cwaViewModels { viewModelFactory }

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

        binding.contactDiaryPersonBottomSheetCloseButton.buttonIcon.setOnClickListener {
            viewModel.closePressed()
        }

        binding.contactDiaryPersonBottomSheetTextInputEditText.doAfterTextChanged {
            viewModel.textChanged(it.toString())
        }

        binding.contactDiaryPersonBottomSheetTextInputEditText.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> false
                else -> true
            }
        }

        viewModel.shouldClose.observe2(this) {
            dismiss()
        }

        viewModel.isValid.observe2(this) {
            binding.contactDiaryPersonBottomSheetTextInputLayout.isErrorEnabled = it
            binding.contactDiaryPersonBottomSheetSaveButton.isEnabled = it
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
