package de.rki.coronawarnapp.test.datadonation.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestDatadonationBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.json.JSONObject
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class DataDonationTestFragment : Fragment(R.layout.fragment_test_datadonation), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: DataDonationTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestDatadonationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.currentReport.observe2(this) {
            binding.safetynetBody.text = it?.body?.toString()?.let { json ->
                JSONObject(json).toString(4)
            }
        }

        binding.apply {
            safetynetCreateReport.setOnClickListener { vm.createSafetyNetReport() }
            safetynetCopyJws.setOnClickListener { vm.copyJWS() }
        }

        vm.copyJWSEvent.observe2(this) { jws ->
            val intent = ShareCompat.IntentBuilder.from(requireActivity()).apply {
                setType("text/plain")
                setSubject("JWS")
                setText(jws)
            }.createChooserIntent()
            startActivity(intent)
        }

        vm.currentValidation.observe2(this) { items ->
            if (items?.first == null) {
                binding.safetynetRequirementsBody.text = "No validation yet."
                return@observe2
            }
            binding.safetynetRequirementsBody.apply {
                text = items.first.toString()
                append("\n\n")
                if (items.second != null) {
                    append(items.second.toString())
                } else {
                    append("Requirements passed!")
                }
            }
        }
        binding.apply {
            safetynetRequirementsCasually.setOnClickListener { vm.validateSafetyNetCasually() }
            safetynetRequirementsStrict.setOnClickListener { vm.validateSafetyNetStrict() }
        }

        vm.errorEvents.observe2(this) {
            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Data Donation",
            description = "SafetyNet, Analytics, Surveys et al.",
            targetId = R.id.test_datadonation_fragment
        )
    }
}
