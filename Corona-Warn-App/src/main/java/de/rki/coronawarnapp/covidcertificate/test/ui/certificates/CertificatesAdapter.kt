package de.rki.coronawarnapp.covidcertificate.test.ui.certificates

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.cards.CovidTestCertificateCard
import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.cards.CovidTestCertificateErrorCard
import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.CreateVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.HeaderInfoVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.ImmuneVaccinationCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.NoCovidTestCertificatesCard
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards.VaccinationCard
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class CertificatesAdapter :
    ModularAdapter<CertificatesAdapter.CertificatesItemVH<CertificatesItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<CertificatesItem> {

    override val asyncDiffer: AsyncDiffer<CertificatesItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<CertificatesItem, CertificatesItemVH<CertificatesItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is ImmuneVaccinationCard.Item }) {
                    ImmuneVaccinationCard(it)
                },
                TypedVHCreatorMod({ data[it] is VaccinationCard.Item }) {
                    VaccinationCard(it)
                },
                TypedVHCreatorMod({ data[it] is CreateVaccinationCard.Item }) { CreateVaccinationCard(it) },
                TypedVHCreatorMod({ data[it] is HeaderInfoVaccinationCard.Item }) { HeaderInfoVaccinationCard(it) },
                TypedVHCreatorMod({ data[it] is NoCovidTestCertificatesCard.Item }) { NoCovidTestCertificatesCard(it) },
                TypedVHCreatorMod({ data[it] is CovidTestCertificateCard.Item }) { CovidTestCertificateCard(it) },
                TypedVHCreatorMod({ data[it] is CovidTestCertificateErrorCard.Item }) {
                    CovidTestCertificateErrorCard(it)
                },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class CertificatesItemVH<Item : CertificatesItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
