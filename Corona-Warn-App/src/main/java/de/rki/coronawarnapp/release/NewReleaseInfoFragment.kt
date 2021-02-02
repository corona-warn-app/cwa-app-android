package de.rki.coronawarnapp.release

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.NewReleaseInfoItemBinding
import de.rki.coronawarnapp.databinding.NewReleaseInfoScreenFragmentBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class NewReleaseInfoFragment : Fragment(R.layout.new_release_info_screen_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val vm: NewReleaseInfoViewModel by cwaViewModels { viewModelFactory }
    private val binding: NewReleaseInfoScreenFragmentBinding by viewBindingLazy()
    private val args: NewReleaseInfoFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            newReleaseInfoNextButton.setOnClickListener {
                vm.onNextButtonClick()
            }

            toolbar.setNavigationOnClickListener {
                vm.onNextButtonClick()
            }

            headline.text = vm.title.get(requireContext())

            newReleaseInfoNextButton.isGone = args.comesFromInfoScreen

            if (args.comesFromInfoScreen) {
                toolbar.setNavigationIcon(R.drawable.ic_back)
            }

            recyclerView.adapter = ItemAdapter(getItems())
        }

        vm.routeToScreen.observe2(this) {
            if (it is NewReleaseInfoNavigationEvents.CloseScreen) {
                popBackStack()
            }
        }
    }

    private fun getItems(): List<NewReleaseInfoItem> {
        val titles = resources.getStringArray(R.array.new_release_title)
        val textBodies = resources.getStringArray(R.array.new_release_body)
        return vm.getItems(titles, textBodies)
    }

    override fun onResume() {
        super.onResume()
        binding.container.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}

private class ItemAdapter(
    private val items: List<NewReleaseInfoItem>
) : BaseAdapter<ItemAdapter.ViewHolder>() {

    inner class ViewHolder(parent: ViewGroup) : BaseAdapter.VH(R.layout.new_release_info_item, parent),
        BindableVH<NewReleaseInfoItem, NewReleaseInfoItemBinding> {
        override val viewBinding:
            Lazy<NewReleaseInfoItemBinding> =
            lazy { NewReleaseInfoItemBinding.bind(itemView) }

        override val onBindData:
            NewReleaseInfoItemBinding.(item: NewReleaseInfoItem, payloads: List<Any>) -> Unit =
            { item, _ ->
                title.text = item.title
                body.text = item.body
            }
    }

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun onBindBaseVH(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(items[position], payloads)
    }

    override fun getItemCount() = items.size
}
