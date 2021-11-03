package de.rki.coronawarnapp.reyclebin.ui.adapter

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.RecyclerBinOverviewListSubheaderBinding

class OverviewSubHeaderVH(parent: ViewGroup) :
    RecyclerBinAdapter.ItemVH<OverviewSubHeaderItem, RecyclerBinOverviewListSubheaderBinding>(
        layoutRes = R.layout.recycler_bin_overview_list_subheader,
        parent = parent
    ) {

    override val viewBinding: Lazy<RecyclerBinOverviewListSubheaderBinding> =
        lazy { RecyclerBinOverviewListSubheaderBinding.bind(itemView) }

    override val onBindData: RecyclerBinOverviewListSubheaderBinding.(
        item: OverviewSubHeaderItem,
        payloads: List<Any>
    ) -> Unit = { _, _ ->
        // NOOP
    }
}
