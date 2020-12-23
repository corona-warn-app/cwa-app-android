package de.rki.coronawarnapp.test.menu.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestMenuBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class TestMenuFragment : Fragment(R.layout.fragment_test_menu), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: TestMenuFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestMenuBinding by viewBindingLazy()

    @Inject lateinit var menuAdapter: TestMenuAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.testMenuList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = menuAdapter
        }

        vm.testMenuData.observe2(this) { menuAdapter.data = it }
        menuAdapter.onItemClickListener = { vm.showTestScreen(it) }

        vm.showTestScreenEvent.observe2(this) {
            findNavController().navigate(it.targetId)
        }
    }
}
