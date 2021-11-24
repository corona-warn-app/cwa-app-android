package de.rki.coronawarnapp.dccticketing.ui.shared

import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class DccTicketingSharedViewModelTest : BaseTest() {

    private val instance: DccTicketingSharedViewModel
        get() = DccTicketingSharedViewModel()

    @Test
    fun `Init with empty transaction context flow`() = runBlockingTest2(ignoreActive = true) {
        shouldThrow<NoSuchElementException> { instance.transactionContext.first() }
    }

    @Test
    fun `Update transaction context`() = runBlockingTest2(ignoreActive = true) {
        val data = DccTicketingQrCodeData(
            protocol = "Test protocol",
            protocolVersion = "Test protocolVersion",
            serviceIdentity = "Test serviceIdentity",
            privacyUrl = "https://www.test.de/",
            token = "Test token",
            consent = "Test consent",
            subject = "Test subject",
            serviceProvider = "Test serviceProvider"
        )

        val ctx = DccTicketingTransactionContext(initializationData = data)

        instance.run {
            updateTransactionContext(ctx = ctx)
            transactionContext.first() shouldBe ctx

            val updatedCtx = ctx.copy(
                initializationData = ctx.initializationData.copy(protocol = "Test protocol updated")
            )
            updateTransactionContext(ctx = updatedCtx)
            transactionContext.first() shouldBe updatedCtx
        }
    }
}
