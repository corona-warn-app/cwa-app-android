package de.rki.coronawarnapp.contactdiary.ui.day.sheets.person

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.rki.coronawarnapp.databinding.ContactDiaryPersonBottomSheetFragmentBinding
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ContactDiaryPersonBottomSheetFragmentBinding.inflate(inflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.contactDiaryPersonBottomSheetCloseButton.buttonIcon.setOnClickListener {
            viewModel.closePressed()
        }

        binding.contactDiaryPersonBottomSheetSaveButton.setOnClickListener {
            viewModel.savePerson(
                binding.contactDiaryPersonBottomSheetTextInputEditText.text.toString()
            )
        }

        viewModel.shouldClose.observe2(this) {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
