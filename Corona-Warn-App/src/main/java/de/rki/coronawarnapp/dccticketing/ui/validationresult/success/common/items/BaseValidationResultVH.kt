package de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.items

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter

abstract class BaseValidationResultVH<Item : ValidationResultItem, VB : ViewBinding>(
    @LayoutRes layoutRes: Int,
    parent: ViewGroup
) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
