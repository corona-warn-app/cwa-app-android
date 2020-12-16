package de.rki.coronawarnapp.test.submission.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestSubmissionBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.tracing.ui.TracingConsentDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
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

        val tekHistoryAdapter = TEKHistoryAdapter()
        binding.tekHistoryList.apply {
            adapter = tekHistoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        vm.tekHistory.observe2(this) { teks ->
            tekHistoryAdapter.update(teks)
            binding.tekStorageCount.text = "${teks.size} TEKs"
        }

        vm.shareTEKsEvent.observe2(this) { tekExport ->
            val share = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, tekExport.exportText)
            }, null)
            startActivity(share)
        }

        binding.apply {
            tekStorageUpdate.setOnClickListener { vm.updateStorage() }
            tekStorageClear.setOnClickListener { vm.clearStorage() }
            tekStorageEmail.setOnClickListener { vm.emailTEKs() }
        }
        vm.permissionRequestEvent.observe2(this) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }
        vm.showTracingConsentDialog.observe2(this) { consentResult ->
            TracingConsentDialog(requireContext()).show(
                onConsentGiven = { consentResult(true) },
                onConsentDeclined = { consentResult(false) }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!vm.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
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
