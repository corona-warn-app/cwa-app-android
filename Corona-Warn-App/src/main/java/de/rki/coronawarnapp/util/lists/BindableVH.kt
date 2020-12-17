package de.rki.coronawarnapp.util.lists

import androidx.viewbinding.ViewBinding

interface BindableVH<ItemT, ViewBindingT : ViewBinding> {

    val viewBinding: Lazy<ViewBindingT>

    val onBindData: ViewBindingT.(item: ItemT, payloads: List<Any>) -> Unit

    fun bind(item: ItemT, payloads: MutableList<Any> = mutableListOf()) = with(viewBinding.value) {
        onBindData(item, payloads)
    }
}
