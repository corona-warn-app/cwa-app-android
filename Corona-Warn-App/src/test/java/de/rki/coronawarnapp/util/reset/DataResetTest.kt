package de.rki.coronawarnapp.util.reset

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DataResetTest : BaseTest() {

    private fun createResettableSet() = setOf<Resettable>(
        mockk(),
        mockk(),
        mockk(),
        mockk(),
    )

    @Test
    fun `resets ever resettable`() = runTest {
        val resettableSet = createResettableSet()
        DataReset(resettableDataProvider = { resettableSet }).clearAllLocalData()

        coVerify {
            resettableSet.forEach { it.reset() }
        }
    }

    @Test
    fun `errors do not interrupt resetting`() = runTest {
        val resettableSet = createResettableSet()

        resettableSet.forEach {
            coEvery { it.reset() } throws Exception("Test error")
        }

        DataReset(resettableDataProvider = { resettableSet }).clearAllLocalData()

        coVerify {
            resettableSet.forEach { it.reset() }
        }
    }
}
