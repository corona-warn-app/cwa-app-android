package de.rki.coronawarnapp.miscinfo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestDeviceinfoBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class MiscInfoFragment : Fragment(R.layout.fragment_test_deviceinfo), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: MiscInfoFragmentViewModel by cwaViewModels { viewModelFactory }

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
