package de.rki.coronawarnapp.test.appconfig.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestAppconfigBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class AppConfigTestFragment : Fragment(R.layout.fragment_test_appconfig), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: AppConfigTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestAppconfigBinding by viewBinding()

    private val timeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd - HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.currentConfig.observe(viewLifecycleOwner) { data ->
            binding.currentConfiguration.text = data.toString()
            binding.lastUpdate.text = timeFormatter.format(data.updatedAt)
            binding.timeOffset.text =
                """
            ${data.localOffset.toMillis()}ms
            configType=${data.configType}
            isDeviceTimeCorrect=${data.isDeviceTimeCorrect}
                """.trimIndent()
        }

        vm.errorEvent.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_LONG).show()
        }

        binding.downloadAction.setOnClickListener { vm.download() }
        binding.deleteAction.setOnClickListener { vm.clearConfig() }

        vm.isDeviceTimeFaked.observe(viewLifecycleOwner) {
            binding.fakeCorrectDevicetimeToggle.isChecked = it
        }
        binding.fakeCorrectDevicetimeToggle.setOnClickListener {
            vm.toggleFakeCorrectDeviceTime()
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Remote Config Data",
            description = "View & Control the remote config.",
            targetId = R.id.test_appconfig_fragment
        )
    }
}
