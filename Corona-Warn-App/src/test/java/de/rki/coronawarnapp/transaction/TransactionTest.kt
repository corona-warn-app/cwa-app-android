package de.rki.coronawarnapp.transaction

import de.rki.coronawarnapp.exception.RollbackException
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.risk.TimeVariables
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.types.beInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.IOException

@ExperimentalCoroutinesApi
class TransactionTest : BaseTest() {

    private val testScope = TestCoroutineScope()

    @BeforeEach
    fun setup() {
        mockkObject(TimeVariables)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Suppress("UNREACHABLE_CODE")
    private class TestTransaction(
        val errorOnRollBack: Exception? = null
    ) : Transaction() {
        override val TAG: String = "TestTag"

        public override suspend fun rollback() {
            errorOnRollBack?.let { handleRollbackError(it) }
            super.rollback()
        }

        public override suspend fun handleTransactionError(error: Throwable): Nothing {
            return super.handleTransactionError(error)
        }

        public override fun handleRollbackError(error: Throwable?): Nothing {
            return super.handleRollbackError(error)
        }
    }

    @Test
    fun `transaction error handler is called`() {
        val testTransaction = spyk(TestTransaction())
        shouldThrow<TransactionException> {
            runBlocking {
                testTransaction.lockAndExecute(scope = testScope) {
                    throw IOException()
                }
            }
        }

        coVerify { testTransaction.handleTransactionError(any()) }
        coVerify { testTransaction.rollback() }
    }

    @Test
    fun `rollback error handler is called`() {
        val testTransaction = spyk(
            TestTransaction(
                errorOnRollBack = IllegalAccessException()
            )
        )
        shouldThrow<RollbackException> {
            runBlocking {
                testTransaction.lockAndExecute(scope = testScope) {
                    throw IOException()
                }
            }
        }

        coVerify { testTransaction.handleTransactionError(ofType<IOException>()) }
        coVerify { testTransaction.rollback() }
        coVerify { testTransaction.handleRollbackError(ofType<IllegalAccessException>()) }
    }

    @Test
    fun `transactions can timeout`() {
        // TODO use a test scope and advance time
        every { TimeVariables.getTransactionTimeout() } returns 0L

        val testTransaction = TestTransaction()
        val exception = shouldThrow<TransactionException> {
            runBlocking {
                testTransaction.lockAndExecute(scope = testScope) {
                    delay(2000)
                }
            }
        }
        exception.cause should beInstanceOf<TimeoutCancellationException>()
    }
}
