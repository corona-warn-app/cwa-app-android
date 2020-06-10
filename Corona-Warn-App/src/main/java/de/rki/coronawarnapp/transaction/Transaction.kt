/******************************************************************************
 * Corona-Warn-App                                                            *
 *                                                                            *
 * SAP SE and all other contributors /                                        *
 * copyright owners license this file to you under the Apache                 *
 * License, Version 2.0 (the "License"); you may not use this                 *
 * file except in compliance with the License.                                *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing,                 *
 * software distributed under the License is distributed on an                *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                     *
 * KIND, either express or implied.  See the License for the                  *
 * specific language governing permissions and limitations                    *
 * under the License.                                                         *
 ******************************************************************************/

package de.rki.coronawarnapp.transaction

import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.exception.RollbackException
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.transaction.Transaction.InternalTransactionStates.INIT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

/**
 * The Transaction is used to define an internal process that can go through various states.
 * It contains a mutex that is used to reference the current coroutine context and also a thread-safe
 * Transaction ID that can be used to identify a transaction instance in the entire system.
 *
 * The Transaction uses an internal State Handling that is defined so that error cases can be caught by state
 *
 * @throws TransactionException An Exception thrown when an error occurs during Transaction Execution
 * @throws RollbackException An Exception might get thrown if rollback behavior is implemented
 */
abstract class Transaction {

    companion object {
        /**
         * Transaction Timeout in Milliseconds, used to cancel any Transactions that run into never ending execution
         * (e.g. due to a coroutine not being cancelled properly or an exception leading to unchecked behavior)
         */
        private val TRANSACTION_TIMEOUT_MS =
            TimeVariables.getTransactionTimeout()
    }

    @Suppress("VariableNaming") // Done as the Convention is TAG for every class
    abstract val TAG: String?

    /**
     * This is the State Stack that is used inside the Transaction. It is an atomic reference held only by the
     * internal transaction and cannot be interacted with directly to ensure atomic operation.
     *
     * It is modified by executing a state. It will contain the latest state after execution of a state.
     *
     * @see executeState
     * @see resetExecutedStateStack
     * @see getExecutedStates
     *
     * @see finalizeState
     * @see setState
     * @see currentTransactionState
     * @see isInStateStack
     */
    private val executedStatesStack: AtomicReference<MutableList<TransactionState>> =
        AtomicReference(ArrayList())

    /**
     * Finalizes a state by adding the state to the executedStatesStack
     */
    private fun finalizeState() = executedStatesStack.get().add(currentTransactionState.get())

    /**
     * Sets the transaction state and logs the state change.
     *
     * @param state the new transaction state
     */
    private fun setState(state: TransactionState) =
        currentTransactionState.set(state)
            .also {
                Timber.d("$transactionId - STATE CHANGE: ${currentTransactionState.get()}")
            }

    /**
     * The atomic Transaction ID that should be set during Transaction Start. Used to identify execution context and errors.
     */
    protected val transactionId = AtomicReference<UUID>()

    /**
     * The mutual exclusion lock used to handle the lock during the execution across contexts.
     */
    private val internalMutualExclusionLock = Mutex()

    /**
     * The atomic Transaction State that should be set during Transaction steps.
     * It should be updated by the implementing Transaction.
     */
    private val currentTransactionState = AtomicReference<TransactionState>()

    /**
     * Checks if a given Transaction State is in the current Stack Trace.
     *
     * @see executedStatesStack
     */
    protected fun TransactionState.isInStateStack() = executedStatesStack.get().contains(this)

    /**
     * Checks for all already executed states from the state stack.
     *
     * @return list of all executed states
     * @see executedStatesStack
     */
    private fun getExecutedStates() = executedStatesStack.get().toList()

    /**
     * Resets the state stack. this method needs to be called after successful transaction execution in order to
     * not contain any states from a previous transaction
     *
     * @see executedStatesStack
     */
    private fun resetExecutedStateStack() = executedStatesStack.get().clear()

    /**
     * Executes a given state and sets it as the active State, then executing the coroutine that should
     * be called for this state, and then finalizing the state by adding the state to the executedStatesStack.
     *
     * Should an error occur during the state execution, an exception can take a look at the currently executed state
     * as well as the transaction ID to refer to the concrete Error case.
     *
     * @param T The generic Return Type used for typing the state return value.
     * @param context The context used to spawn the coroutine in
     * @param state The state that should be executed and added to the state stack.
     * @param block Any function containing the actual Execution Code for that state
     * @return The return value of the state, useful for piping to a wrapper or a lock without a message bus or actor
     */
    private suspend fun <T> executeState(
        context: CoroutineContext,
        state: TransactionState,
        block: suspend CoroutineScope.() -> T
    ): T = withContext(context) {
        setState(state)
        val result = block.invoke(this)
        finalizeState()
        return@withContext result
    }

    /**
     * Convenience method to call for a state execution with the Default Dispatcher. For more details, refer to
     * the more detailed executeState that this call wraps around.
     *
     * @see executeState
     * @param T The generic Return Type used for typing the state return value.
     * @param state The state that should be executed and added to the state stack.
     * @param block Any function containing the actual Execution Code for that state
     * @return The return value of the state, useful for piping to a wrapper or a lock without a message bus or actor
     */
    protected suspend fun <T> executeState(
        state: TransactionState,
        block: suspend CoroutineScope.() -> T
    ): T =
        executeState(Dispatchers.Default, state, block)

    /**
     * Executes the transaction as Unique. This results in the next execution being omitted in case of a race towards
     * the lock. Please see [lockAndExecute] for more details
     *
     * @param T Optional Return Type in case the Transaction should return a value from the coroutine.
     * @param block the suspending function that should be used to execute the transaction.
     */
    protected suspend fun <T> lockAndExecuteUnique(block: suspend CoroutineScope.() -> T) =
        lockAndExecute(true, block)

    /**
     * Executes the transaction as non-unique. This results in the next execution being queued in case of a race towards
     * the lock. Please see [lockAndExecute] for more details
     *
     * @param T Optional Return Type in case the Transaction should return a value from the coroutine.
     * @param block the suspending function that should be used to execute the transaction.
     */
    protected suspend fun <T> lockAndExecute(block: suspend CoroutineScope.() -> T) =
        lockAndExecute(false, block)

    /**
     * Attempts to go into the internal lock context (mutual exclusion coroutine) and executes the given suspending
     * function. Standard Logging is executed to inform about the transaction status.
     * The Lock will run under the Timeout defined under TRANSACTION_TIMEOUT_MS. If the coroutine executed during this
     * transaction does not returned within the specified timeout, an error will be thrown.
     *
     * After invoking the suspending function, the internal state stack will be reset for the next execution.
     *
     * Inside the given function one should execute executeState() as this will set the Transaction State accordingly
     * and allow for atomic rollbacks.
     *
     * In an error scenario, during the handling of the transaction error, a rollback will be executed on best-effort basis.
     *
     * @param T Optional Return Type in case the Transaction should return a value from the coroutine.
     * @param block the suspending function that should be used to execute the transaction.
     * @throws TransactionException the exception that wraps around any error that occurs inside the lock.
     *
     * @see executeState
     * @see executedStatesStack
     */
    private suspend fun <T> lockAndExecute(unique: Boolean, block: suspend CoroutineScope.() -> T) {
        if (unique && internalMutualExclusionLock.isLocked) {
            val runningString = "TRANSACTION WITH ID $transactionId ALREADY RUNNING " +
                    "($currentTransactionState) AS UNIQUE, SKIPPING EXECUTION."
            Timber.w(runningString)
            return
        }
        try {
            return internalMutualExclusionLock.withLock {
                executeState(INIT) { transactionId.set(UUID.randomUUID()) }
                measureTimeMillis {
                    withTimeout(TRANSACTION_TIMEOUT_MS) {
                        block.invoke(this)
                    }
                }.also {
                    val completedString =
                        "TRANSACTION $transactionId COMPLETED (${System.currentTimeMillis()}) " +
                                "in $it ms, STATES EXECUTED: ${getExecutedStates()}"
                    Timber.i(completedString)
                }
                resetExecutedStateStack()
            }
        } catch (e: Exception) {
            handleTransactionError(e)
        }
    }

    private enum class InternalTransactionStates : TransactionState {
        INIT
    }

    /**
     * Handles the Transaction Error by performing a rollback, resetting the state stack for consistency and then
     * throwing a Transaction Exception with the given error as cause
     *
     * @throws TransactionException an error containing the cause of the transaction failure as cause, if provided
     *
     * @param error the error that lead to an error case in the transaction that cannot be handled inside the
     * transaction but has to be caught from the exception caller
     */
    protected open suspend fun handleTransactionError(error: Throwable?): Nothing {
        rollback()
        resetExecutedStateStack()
        throw TransactionException(
            transactionId.get(),
            currentTransactionState.toString(),
            error
        )
    }

    /**
     * Initiates rollback based on the atomic rollback value references inside the transaction and the current stack.
     * It is called during the Handling of Transaction Errors and should call handleRollbackError on an error case.
     *
     * Atomic references towards potential rollback targets need to be kept in the concrete Transaction Implementation,
     * as this can differ on a case by case basis. Nevertheless, rollback will always be executed, no matter if an
     * override is provided or not
     *
     * @throws RollbackException throws a rollback exception when handleRollbackError() is called
     */
    protected open suspend fun rollback() {
        if (BuildConfig.DEBUG) Timber.d("Initiate Rollback")
    }

    /**
     * Handles the Rollback Error by throwing a RollbackException with the given error as cause
     *
     * @throws TransactionException an error containing the cause of the rollback failure as cause, if provided
     *
     * @param error the error that lead to an error case in the rollback
     */
    protected open fun handleRollbackError(error: Throwable?): Nothing {
        throw RollbackException(
            transactionId.get(),
            currentTransactionState.toString(),
            error
        )
    }
}
