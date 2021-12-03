package de.rki.coronawarnapp.dccticketing.core.allowlist.repo.storage

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingAllowListContainer
import javax.inject.Inject

class DccTicketingAllowListStorage @Inject constructor() {

    fun load(): DccTicketingAllowListContainer = DccTicketingAllowListContainer()

    fun save(value: DccTicketingAllowListContainer) {}
}
