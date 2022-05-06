package de.rki.coronawarnapp.dccticketing.core.reset

import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.DccTicketingAllowListRepository
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.storage.DccTicketingAllowListStorage
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeSettings
import de.rki.coronawarnapp.util.reset.Resettable
import javax.inject.Inject

class DccTicketingReset @Inject constructor(
    private val dccTicketingAllowListRepository: DccTicketingAllowListRepository,
    private val dccTicketingAllowListStorage: DccTicketingAllowListStorage,
    private val dccTicketingQrCodeSettings: DccTicketingQrCodeSettings
) : Resettable {

    override suspend fun reset() {
        dccTicketingAllowListRepository.reset()
        dccTicketingAllowListStorage.reset()
        dccTicketingQrCodeSettings.reset()
    }
}
