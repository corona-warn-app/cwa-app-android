package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day

import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest.CoronaTestItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskenf.RiskEnfItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent.RiskEventItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.submission.SubmissionItem
import java.time.LocalDate

data class DayOverviewItem(
    val date: LocalDate,
    val riskEnfItem: RiskEnfItem? = null,
    val riskEventItem: RiskEventItem? = null,
    val contactItem: ContactItem? = null,
    val coronaTestItem: CoronaTestItem? = null,
    val submissionItem: SubmissionItem? = null,
    val onItemSelectionListener: (DayOverviewItem) -> Unit
) : DiaryOverviewItem {
    override val stableId: Long = date.hashCode().toLong()
}
