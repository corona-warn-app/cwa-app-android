package de.rki.coronawarnapp.test.keydownload.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestKeydownloadBinding
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class KeyDownloadTestFragment : Fragment(R.layout.fragment_test_keydownload), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: KeyDownloadTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestKeydownloadBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.fakeMeteredConnection.observe2(this) {
            binding.fakeMeteredConnectionToggle.isChecked = it
        }
        binding.fakeMeteredConnectionToggle.setOnClickListener { vm.toggleAllowMeteredConnections() }

        vm.isMeteredConnection.observe2(this) {
            binding.infoMeteredNetwork.text = "Is metered network? $it"
        }

        binding.apply {
            downloadAction.setOnClickListener { vm.download() }
            clearAction.setOnClickListener { vm.clearDownloads() }
        }

        vm.isSyncRunning.observe2(this) { isRunning ->
            binding.apply {
                downloadAction.isEnabled = !isRunning
                clearAction.isEnabled = !isRunning
            }
        }

        val keyFileAdapter = KeyFileDownloadAdapter { vm.deleteKeyFile(it) }
        binding.cacheList.apply {
            adapter = keyFileAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        vm.currentCache.observe2(this) { items ->
            val dayCount = items.count { it.info.type == CachedKeyInfo.Type.LOCATION_DAY }
            val hourCount = items.count { it.info.type == CachedKeyInfo.Type.LOCATION_HOUR }
            binding.cacheListInfos.text = "${items.size} files, $dayCount days, $hourCount hours."

            keyFileAdapter.update(items)
        }

        vm.errorEvent.observe2(this) {
            Snackbar.make(requireView(), it.toString(), Snackbar.LENGTH_LONG).show()
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Key Packages",
            description = "View & Control the downloaded key pkgs..",
            targetId = R.id.test_keydownload_fragment
        )
    }
}
