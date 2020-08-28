package de.rki.coronawarnapp.transaction

import de.rki.coronawarnapp.exception.RollbackException
import de.rki.coronawarnapp.exception.TransactionException
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.IOException

@ExperimentalCoroutinesApi
class TransactionTest : BaseTest() {

    private val testScope = TestCoroutineScope()

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

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
}
