package de.rki.coronawarnapp.ui.submission.greencertificate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.databinding.FragmentRequestGreenCertificateBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import org.joda.time.LocalDate
import javax.inject.Inject

class RequestGreenCertificateFragment : Fragment(R.layout.fragment_request_green_certificate), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModelsAssisted<RequestGreenCertificateViewModel>(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as RequestGreenCertificateViewModel.Factory
            factory.create(args.testType)
        }
    )
    private val binding by viewBinding<FragmentRequestGreenCertificateBinding>()
    private val args by navArgs<RequestGreenCertificateFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            val isPCR = args.testType == CoronaTest.Type.PCR
            birthDateGroup.isVisible = isPCR
            privacyCard.pcrExtraBullet.isVisible = isPCR

            dateInputEdit.doOnTextChanged { text, _, _, _ ->
                if (text.toString().isEmpty()) viewModel.birthDateChanged(null)
            }

            toolbar.setNavigationOnClickListener { showDialog() }
            agreeButton.setOnClickListener { viewModel.onAgreeGC() }
            disagreeButton.setOnClickListener { viewModel.onDisagreeGC() }
            dateInputEdit.setOnClickListener { openDatePicker() }
        }

    private fun showDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.request_gc_dialog_title)
            .setMessage(R.string.request_gc_dialog_message)
            .setNegativeButton(R.string.request_gc_dialog_negative_button) { _, _ -> /* TODO */ }
            .setPositiveButton(R.string.request_gc_dialog_positive_button) { _, _ -> /* TODO */ }
            .create()
            .show()
    }

    private fun openDatePicker() {
        MaterialDatePicker.Builder
            .datePicker()
            .build()
            .apply {
                addOnPositiveButtonClickListener { timestamp ->
                    val localDate = LocalDate(timestamp)
                    binding.dateInputEdit.setText(localDate.toDayFormat())
                    viewModel.birthDateChanged(localDate)
                }
            }
            .show(childFragmentManager, "RequestGreenCertificateFragment.MaterialDatePicker")
    }
}
