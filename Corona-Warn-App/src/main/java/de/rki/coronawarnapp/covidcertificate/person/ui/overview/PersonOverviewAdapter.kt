package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificatesItem
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod
import timber.log.Timber

class PersonOverviewAdapter :
    ModularAdapter<PersonOverviewAdapter.PersonOverviewItemVH<PersonCertificatesItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<PersonCertificatesItem> {

    override val asyncDiffer: AsyncDiffer<PersonCertificatesItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<PersonCertificatesItem, PersonOverviewItemVH<PersonCertificatesItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is CovidTestCertificatePendingCard.Item }) {
                    CovidTestCertificatePendingCard(it)
                },
                TypedVHCreatorMod({ data[it] is PersonCertificateCard.Item }) { PersonCertificateCard(it) },
            )
        )

        Timber.tag(TAG).d("modules=%s", modules)
    }

    override fun getItemCount(): Int = data.size

    abstract class PersonOverviewItemVH<Item : PersonCertificatesItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>

    companion object {
        private const val TAG = "PersonOverviewAdapter"
    }
}
