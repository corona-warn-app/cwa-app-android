package de.rki.coronawarnapp.submission.ui.testresults.negative

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionAntigenTestResultNegativeBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalTime
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class RATResultNegativeFragment : Fragment(R.layout.fragment_submission_antigen_test_result_negative), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: RATResultNegativeViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionAntigenTestResultNegativeBinding by viewBindingLazy()

    private val shortDate = DateTimeFormat.shortDate()
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
        rapidTestCardPatientName.text = getString(
            R.string.submission_test_result_antigen_patient_name_placeholder,
            testAge.test.firstName,
            testAge.test.lastName
        )

        rapidTestCardPatientBirthdate.text = getString(
            R.string.submission_test_result_antigen_patient_birth_date_placeholder,
            testAge.test.dateOfBirth?.toString(shortDate)
        )

        val localTime = testAge.test.testResultReceivedAt?.toLocalTime()
        resultReceivedTimeAndDate.text = getString(
            R.string.coronatest_negative_antigen_result_time_date_placeholder,
            localTime?.toString(shortDate),
            localTime?.toString(shortTime)
        )
    }
}
