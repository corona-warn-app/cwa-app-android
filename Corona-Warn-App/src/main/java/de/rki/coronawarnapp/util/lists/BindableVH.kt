package de.rki.coronawarnapp.util.lists

import androidx.viewbinding.ViewBinding

interface BindableVH<ItemT, ViewBindingT : ViewBinding> {

    val viewBinding: Lazy<ViewBindingT>

    val onBindData: ViewBindingT.(item: ItemT) -> Unit

    fun bind(item: ItemT) = with(viewBinding.value) { onBindData(item) }
}
