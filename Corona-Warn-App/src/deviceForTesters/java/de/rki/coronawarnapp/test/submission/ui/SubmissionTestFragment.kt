package de.rki.coronawarnapp.test.submission.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestSubmissionBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class SubmissionTestFragment : Fragment(R.layout.fragment_test_submission), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SubmissionTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestSubmissionBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.currentTestId.observe2(this) {
            binding.registrationTokenCurrent.text = "Current: '$it'"
        }

        binding.apply {
            deleteTokenAction.setOnClickListener { vm.deleteRegistrationToken() }
            scrambleTokenAction.setOnClickListener { vm.scrambleRegistrationToken() }
        }
    }

    companion object {
        val TAG: String = SubmissionTestFragment::class.simpleName!!
        val MENU_ITEM = TestMenuItem(
            title = "Submission Test Options",
            description = "Submission related test options..",
            targetId = R.id.test_submission_fragment
        )
    }
}
