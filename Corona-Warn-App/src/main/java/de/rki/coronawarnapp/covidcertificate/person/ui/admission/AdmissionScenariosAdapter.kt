package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.HasStableId
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod
import timber.log.Timber

class AdmissionScenariosAdapter :
    ModularAdapter<AdmissionScenariosAdapter.AdmissionItemVH<AdmissionScenarioItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<AdmissionScenarioItem> {

    override val asyncDiffer: AsyncDiffer<AdmissionScenarioItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<AdmissionScenarioItem, AdmissionItemVH<AdmissionScenarioItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is AdmissionItemCard.Item }) { AdmissionItemCard(it) },
            )
        )

        Timber.tag(TAG).d("modules=%s", modules)
    }

    override fun getItemCount(): Int = data.size

    abstract class AdmissionItemVH<Item : AdmissionScenarioItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>

    companion object {
        private const val TAG = "AdmissionScenariosAdapter"
    }
}

interface AdmissionScenarioItem : HasStableId
