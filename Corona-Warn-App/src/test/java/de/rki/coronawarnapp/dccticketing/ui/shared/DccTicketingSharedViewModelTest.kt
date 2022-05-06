package de.rki.coronawarnapp.dccticketing.ui.shared

import androidx.lifecycle.SavedStateHandle
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel.Companion.TRANSACTION_CONTEXT_SAVED_STATE_KEY
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2

class DccTicketingSharedViewModelTest : BaseTest() {

    private val data = DccTicketingQrCodeData(
        protocol = "Test protocol",
        protocolVersion = "Test protocolVersion",
        serviceIdentity = "Test serviceIdentity",
        privacyUrl = "https://www.test.de/",
        token = "Test token",
        consent = "Test consent",
        subject = "Test subject",
        serviceProvider = "Test serviceProvider"
    )

    private fun createInstance(savedStateHandle: SavedStateHandle = SavedStateHandle()) = DccTicketingSharedViewModel(
        savedState = savedStateHandle
    )

    @Test
    fun `init with empty transaction context flow`() = runTest2 {
        shouldThrow<NoSuchElementException> { createInstance().transactionContext.first() }
    }

    @Test
    fun `restores transaction context from save state handle`() = runTest2 {
        val transactionContext = DccTicketingTransactionContext(initializationData = data)
        val savedStateHandle = SavedStateHandle().apply {
            this[TRANSACTION_CONTEXT_SAVED_STATE_KEY] = transactionContext
        }

        createInstance(savedStateHandle = savedStateHandle).transactionContext.first() shouldBe transactionContext
    }

    @Test
    fun `init and update transaction context`() = runTest2 {
        val savedStateHandle = SavedStateHandle()

        val ctx = DccTicketingTransactionContext(initializationData = data)

        with(createInstance(savedStateHandle = savedStateHandle)) {
            savedStateHandle.getTransactionContext() shouldBe null

            initializeTransactionContext(ctx = ctx)
            transactionContext.first() shouldBe ctx
            savedStateHandle.getTransactionContext() shouldBe ctx

            val updatedData = ctx.initializationData.copy(protocol = "Test protocol updated")
            val updatedCtx = ctx.copy(
                initializationData = updatedData
            )
            updateTransactionContext {
                it.copy(initializationData = updatedData)
            }
            transactionContext.first() shouldBe updatedCtx
            savedStateHandle.getTransactionContext() shouldBe updatedCtx
        }
    }

    @Test
    fun `throws on updating a not initialized transaction context`() = runTest2 {
        val savedStateHandle = SavedStateHandle()

        shouldThrow<IllegalStateException> {
            createInstance(savedStateHandle = savedStateHandle).updateTransactionContext {
                it.copy(initializationData = data)
            }
        }

        savedStateHandle.getTransactionContext() shouldBe null
    }

    private fun SavedStateHandle.getTransactionContext(): DccTicketingTransactionContext? =
        this[TRANSACTION_CONTEXT_SAVED_STATE_KEY]
}
