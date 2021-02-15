package de.rki.coronawarnapp.contactdiary.ui.person

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.focusAndShowKeyboard
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.ContactDiaryAddPersonFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class ContactDiaryAddPersonFragment : Fragment(R.layout.contact_diary_add_person_fragment),
    AutoInject {

    private val binding: ContactDiaryAddPersonFragmentBinding by viewBindingLazy()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ContactDiaryAddPersonViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as ContactDiaryAddPersonViewModel.Factory
            factory.create(navArgs.addedAt)
        }
    )

    private val navArgs: ContactDiaryAddPersonFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val person = navArgs.selectedPerson
        if (person != null) {
            binding.apply {
                contactDiaryPersonNameEditText.setText(person.fullName)
                contactDiaryPersonPhoneNumberEditText.setText(person.phoneNumber)
                contactDiaryPersonEmailEditText.setText(person.emailAddress)
                contactDiaryPersonDeleteButton.visibility = View.VISIBLE
                contactDiaryPersonDeleteButton.setOnClickListener {
                    DialogHelper.showDialog(deletePersonConfirmationDialog)
                }
                contactDiaryPersonSaveButton.setOnClickListener {
                    it.hideKeyboard()
                    viewModel.updatePerson(person)
                }
            }
            viewModel.apply {
                nameChanged(person.fullName)
                person.phoneNumber?.let { phoneNumberChanged(it) }
                person.emailAddress?.let { emailAddressChanged(it) }
            }
        } else {
            binding.contactDiaryPersonDeleteButton.visibility = View.GONE
            binding.contactDiaryPersonSaveButton.setOnClickListener {
                it.hideKeyboard()
                viewModel.addPerson()
            }
        }

        binding.apply {
            contactDiaryPersonNameEditText.focusAndShowKeyboard()

            contactDiaryPersonCloseButton.setOnClickListener {
                it.hideKeyboard()
                viewModel.closePressed()
            }
            contactDiaryPersonNameEditText.doAfterTextChanged {
                viewModel.nameChanged(it.toString())
            }
            contactDiaryPersonPhoneNumberEditText.doAfterTextChanged {
                viewModel.phoneNumberChanged(it.toString())
            }

            contactDiaryPersonEmailEditText.doAfterTextChanged {
                viewModel.emailAddressChanged(it.toString())
            }
            contactDiaryPersonEmailEditText.setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    IME_ACTION_DONE -> {
                        if (viewModel.isNameValid.value == true) {
                            binding.contactDiaryPersonSaveButton.performClick()
                        }
                        false
                    }
                    else -> true
                }
            }
        }

        viewModel.shouldClose.observe2(this) {
            popBackStack()
        }

        viewModel.isNameValid.observe2(this) { isValid ->
            binding.contactDiaryPersonSaveButton.isEnabled = isValid
        }
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
