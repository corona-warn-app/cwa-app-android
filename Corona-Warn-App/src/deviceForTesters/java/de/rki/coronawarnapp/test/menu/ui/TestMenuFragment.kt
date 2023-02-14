package de.rki.coronawarnapp.test.menu.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestMenuBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import javax.inject.Inject

@AndroidEntryPoint
class TestMenuFragment : Fragment(R.layout.fragment_test_menu) {

    private val vm: TestMenuFragmentViewModel by viewModels()
    private val binding: FragmentTestMenuBinding by viewBinding()

    @Inject lateinit var menuAdapter: TestMenuAdapter

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.testMenuList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = menuAdapter
        }

        vm.testMenuData.observe(viewLifecycleOwner) { menuAdapter.data = it }
        menuAdapter.onItemClickListener = { vm.showTestScreen(it) }

        vm.showTestScreenEvent.observe(viewLifecycleOwner) {
            findNavController().navigate(it.targetId)
        }

        vm.personsCount.observe(viewLifecycleOwner) {
            binding.personsCount.text = "Persons count: ${it.first}\nCertificates: ${it.second}"
        }
    }
}
