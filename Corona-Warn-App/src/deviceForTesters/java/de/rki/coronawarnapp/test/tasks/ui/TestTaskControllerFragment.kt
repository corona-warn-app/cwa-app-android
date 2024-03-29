package de.rki.coronawarnapp.test.tasks.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestTaskControllerBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class TestTaskControllerFragment : Fragment(R.layout.fragment_test_task_controller), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: TestTaskControllerFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestTaskControllerBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.factoryState.observe(viewLifecycleOwner) { state ->
            binding.taskfactoriesValues.text = state.infos.joinToString("\n")
        }

        vm.controllerState.observe(viewLifecycleOwner) {
            binding.runningTasksValues.text = it.stateDescriptions.joinToString("\n")
        }

        vm.lastActivityState.observe(viewLifecycleOwner) { state ->
            val lastResults = state.lastActivity.joinToString("\n")
            binding.tasksLastResults.text = lastResults
        }

        vm.latestTestTaskProgress.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            Snackbar.make(
                requireView(),
                "Latest TestTask progress: ${it.primaryMessage.get(requireContext())}",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        binding.testTaskLaunch.setOnClickListener {
            vm.launchTestTask()
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "TaskController",
            description = "Observe and influence the CWA task controller.",
            targetId = R.id.test_taskcontroller_fragment
        )
    }
}
