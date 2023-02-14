package de.rki.coronawarnapp.contactdiary.ui.person

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.focusAndShowKeyboard
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.ContactDiaryAddPersonFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setTextOnTextInput
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryAddPersonFragment :
    Fragment(R.layout.contact_diary_add_person_fragment),
    AutoInject {

    private val binding: ContactDiaryAddPersonFragmentBinding by viewBinding()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryAddPersonViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryAddPersonViewModel.Factory
            factory.create(navArgs.addedAt)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transform = MaterialContainerTransform()
        sharedElementEnterTransition = transform
        sharedElementReturnTransition = transform
    }

    private val navArgs: ContactDiaryAddPersonFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val person = navArgs.selectedPerson
        if (person != null) {
            binding.apply {
                personNameInput.setText(person.fullName)
                personPhoneNumberInput.setTextOnTextInput(person.phoneNumber, endIconVisible = false)
                personEmailInput.setTextOnTextInput(person.emailAddress, endIconVisible = false)
                personDeleteButton.visibility = View.VISIBLE
                personDeleteButton.setOnClickListener { deletePersonConfirmationDialog() }
                personSaveButton.setOnClickListener {
                    viewModel.updatePerson(
                        person,
                        phoneNumber = binding.personPhoneNumberInput.text.toString(),
                        emailAddress = binding.personEmailInput.text.toString()
                    )
                }
            }
            viewModel.nameChanged(person.fullName)
        } else {
            binding.personDeleteButton.visibility = View.GONE
            binding.personSaveButton.setOnClickListener {
                viewModel.addPerson(
                    phoneNumber = binding.personPhoneNumberInput.text.toString(),
                    emailAddress = binding.personEmailInput.text.toString()
                )
            }
        }

        binding.apply {
            personNameInput.focusAndShowKeyboard()

            personCloseButton.setOnClickListener {
                viewModel.closePressed()
            }
            personNameInput.doAfterTextChanged {
                viewModel.nameChanged(it.toString())
            }

            personEmailInput.setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    IME_ACTION_DONE -> {
                        if (viewModel.isNameValid.value == true) {
                            binding.personSaveButton.performClick()
                        }
                        false
                    }
                    else -> true
                }
            }
        }

        viewModel.shouldClose.observe(viewLifecycleOwner) {
            binding.root.hideKeyboard()
            popBackStack()
        }

        viewModel.isNameValid.observe(viewLifecycleOwner) { isValid ->
            binding.personSaveButton.isEnabled = isValid
        }
    }

    private fun deletePersonConfirmationDialog() = displayDialog {
        title(R.string.contact_diary_delete_person_title)
        message(R.string.contact_diary_delete_person_message)
        positiveButton(R.string.contact_diary_delete_button_positive) {
            navArgs.selectedPerson?.let {
                viewModel.deletePerson(it)
            }
        }
        negativeButton(R.string.contact_diary_delete_button_negative)
        setDeleteDialog(true)
    }
}
