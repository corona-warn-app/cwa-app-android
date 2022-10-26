package de.rki.coronawarnapp.dccreissuance.ui.consent

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod
import timber.log.Timber

class DccReissuanceAdapter :
    ModularAdapter<DccReissuanceAdapter.ItemVH<DccReissuanceItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<DccReissuanceItem> {

    override val asyncDiffer: AsyncDiffer<DccReissuanceItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<DccReissuanceItem, ItemVH<DccReissuanceItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is DccReissuanceCertificateCard.Item }) {
                    DccReissuanceCertificateCard(it)
                }
            )
        )

        Timber.tag(TAG).d("modules=%s", modules)
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item : DccReissuanceItem, ViewBinding : androidx.viewbinding.ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, ViewBinding>

    companion object {
        private const val TAG = "DccReissuanceItem"
    }
}
