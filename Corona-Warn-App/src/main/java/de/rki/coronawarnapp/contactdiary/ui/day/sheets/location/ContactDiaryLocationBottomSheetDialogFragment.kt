package de.rki.coronawarnapp.contactdiary.ui.day.sheets.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.rki.coronawarnapp.databinding.ContactDiaryLocationBottomSheetFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ContactDiaryLocationBottomSheetDialogFragment : BottomSheetDialogFragment(), AutoInject {
    private var _binding: ContactDiaryLocationBottomSheetFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryLocationBottomSheetDialogViewModel by cwaViewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ContactDiaryLocationBottomSheetFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.contactDiaryLocationBottomSheetCloseButton.buttonIcon.setOnClickListener {
            viewModel.closePressed()
        }

        binding.contactDiaryLocationBottomSheetSaveButton.setOnClickListener {
            viewModel.saveLocation()
        }

        binding.contactDiaryLocationBottomSheetTextInputEditText.doAfterTextChanged {
            viewModel.textChanged(it.toString())
        }

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
}
