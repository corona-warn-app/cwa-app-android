package de.rki.coronawarnapp.submission.ui.testresults.negative

import android.os.Bundle
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionAntigenTestResultNegativeBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class RATResultNegativeFragment : Fragment(R.layout.fragment_submission_antigen_test_result_negative), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: RATResultNegativeViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionAntigenTestResultNegativeBinding by viewBinding()

    private val shortTime = DateTimeFormat.shortTime()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) =
        with(binding) {
            coronatestNegativeAntigenResultButton.setOnClickListener { viewModel.deleteTest() }
            toolbar.setNavigationOnClickListener { viewModel.onClose() }

            viewModel.testAge.observe(viewLifecycleOwner) {
                it?.let { bindView(it) }
            }

            viewModel.events.observe(viewLifecycleOwner) {
                when (it) {
                    RATResultNegativeNavigation.Back -> popBackStack()
                }
            }
        }

    private fun FragmentSubmissionAntigenTestResultNegativeBinding.bindView(
        testAge: RATResultNegativeViewModel.TestAge
    ) {
        resultReceivedCounter.chronometer.text = testAge.ageText

        val patientName = getString(
            R.string.submission_test_result_antigen_patient_name_placeholder,
            testAge.test.firstName ?: "",
            testAge.test.lastName ?: ""
        )

        rapidTestCardPatientInfo.text = buildSpannedString {
            bold {
                if (patientName.isNotBlank()) append(patientName)
            }
            testAge.test.dateOfBirth?.let {
                val birthDate = getString(
                    R.string.submission_test_result_antigen_patient_birth_date_placeholder,
                    it.toString(DATE_FORMAT)
                )
                if (this.isNotBlank()) append(", ")
                append(birthDate)
            }
        }

        val localTime = testAge.test.testedAt.toUserTimeZone()
        resultReceivedTimeAndDate.text = getString(
            R.string.coronatest_negative_antigen_result_time_date_placeholder,
            localTime?.toString(DATE_FORMAT),
            localTime?.toString(shortTime)
        )
    }

    companion object {
        private const val DATE_FORMAT = "dd.MM.yyyy"
    }
}
