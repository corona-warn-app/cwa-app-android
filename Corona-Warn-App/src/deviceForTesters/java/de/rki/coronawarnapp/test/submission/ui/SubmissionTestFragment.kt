package de.rki.coronawarnapp.test.submission.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.databinding.FragmentTestSubmissionBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.tracing.ui.tracingConsentDialog
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.HashExtensions.toHexString
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import java.time.Instant
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class SubmissionTestFragment : Fragment(R.layout.fragment_test_submission), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SubmissionTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestSubmissionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            val share = Intent.createChooser(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, tekExport.exportText)
                },
                null
            )
            startActivity(share)
        }

        vm.errorEvents.observe2(this) { displayDialog { setError(it) } }

        binding.apply {
            tekRetrieval.setOnClickListener { vm.updateStorage() }
            tekEmail.setOnClickListener { vm.emailTEKs() }
        }
        vm.permissionRequestEvent.observe2(this) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }
        vm.showTracingConsentDialog.observe2(this) { consentResult ->
            tracingConsentDialog(positiveButton = { consentResult(true) }, negativeButton = { consentResult(false) })
        }
        vm.otpData.observe(viewLifecycleOwner) {
            binding.srsOtp.text = it?.let { "OTP:\nUUID=%s\nExpiresAt=%s".format(it.uuid, it.expiresAt) } ?: "No OTP"
        }
        vm.mostRecentSubmissionDate.observe(viewLifecycleOwner) {
            binding.submissionTime.text = "SubmissionTime: %s".format(
                it.takeIf { it != Instant.EPOCH }?.toString() ?: "No Submission"
            )
        }

        binding.submit.setOnClickListener { vm.submit() }

        binding.clearSrsSettings.setOnClickListener {
            vm.resetMostRecentSubmission()
        }
        vm.srsSubmissionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Error -> displayDialog { setError(result.cause) }
                Success -> Toast.makeText(requireContext(), "SRS submission is successful", Toast.LENGTH_LONG).show()
            }
        }

        vm.androidId.observe(viewLifecycleOwner) {
            binding.androidId.text = it.run {
                "AndroidId:\nHex=%s\nBytes=%s".format(toByteArray().toHexString(), toByteArray().joinToString())
            }
        }

        vm.checkLocalPrerequisites.observe(viewLifecycleOwner) { binding.checkDeviceTimeSwitch.isChecked = it }
        binding.checkDeviceTimeSwitch.setOnCheckedChangeListener { _, isChecked ->
            vm.checkLocalPrerequisites(isChecked)
        }

        vm.forceAndroidIdAcceptance.observe(viewLifecycleOwner) { binding.androidIdSwitch.isChecked = it }
        binding.androidIdSwitch.setOnCheckedChangeListener { _, isChecked ->
            vm.forceAndroidIdAcceptance(isChecked)
        }

        binding.clearOtp.setOnClickListener {
            vm.resetOtp()
        }

        val states = ConfigData.DeviceTimeState.values().map { it.key }.toMutableList().apply {
            add(0, "RESET")
        }.toTypedArray()

        binding.deviceTimeState.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, states)
        )
        binding.deviceTimeState.setOnItemClickListener { _, _, position, _ ->
            vm.deviceTimeState(ConfigData.DeviceTimeState.values().find { it.key == states[position] })
        }

        vm.deviceTimeState.observe(viewLifecycleOwner) { state ->
            binding.deviceTimeState.setText(state?.key ?: "RESET", false)
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!vm.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        val TAG: String = SubmissionTestFragment::class.simpleName!!
        val MENU_ITEM = TestMenuItem(
            title = "Submission Test Options",
            description = "Submission related test options.",
            targetId = R.id.test_submission_fragment
        )
    }
}
