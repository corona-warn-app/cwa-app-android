package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CertificatesItem
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.HasStableId
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod

class PersonDetailsAdapter : ModularAdapter<PersonDetailsAdapter.PersonDetailsItemVH<CwaCovidCertificate, ViewBinding>>(),
    AsyncDiffUtilAdapter<CertificatesItem> {

    override val asyncDiffer: AsyncDiffer<CertificatesItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class PersonDetailsItemVH<Item : CwaCovidCertificate, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}

