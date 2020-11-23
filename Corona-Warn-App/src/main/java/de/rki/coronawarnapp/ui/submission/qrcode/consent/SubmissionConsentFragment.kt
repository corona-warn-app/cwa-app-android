package de.rki.coronawarnapp.ui.submission.qrcode.consent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionConsentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionConsentFragment : Fragment(R.layout.fragment_submission_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionConsentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionConsentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
        viewModel.countries.observe2(this) {
            binding.countries = it
        }
    }

    private fun setButtonOnClickListener() {
        binding.submissionConsentButton.setOnClickListener {
            viewModel.onConsentButtonClick()
            doNavigate(
                SubmissionConsentFragmentDirections.actionSubmissionConsentFragmentToSubmissionQRCodeScanFragment()
            )
        }
        binding.submissionConsentHeader.headerButtonBack.buttonIcon.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
