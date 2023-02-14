package de.rki.coronawarnapp.miscinfo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestDeviceinfoBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class MiscInfoFragment : Fragment(R.layout.fragment_test_deviceinfo) {
    private val vm: MiscInfoFragmentViewModel by viewModels()

    private val binding: FragmentTestDeviceinfoBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.errorEvent.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_INDEFINITE).show()
        }

        vm.versionState.observe(viewLifecycleOwner) {
            binding.googlePlayServicesVersionInfo.text = "Google Play Services: ${it.gmsVersion}"
            binding.exposureNotificationServiceVersionInfo.text = "Exposure Notification Services: ${it.enfVersion}"
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Device Infos options",
            description = "GMS/ENF Versions",
            targetId = R.id.miscInfoFragment
        )
    }
}
