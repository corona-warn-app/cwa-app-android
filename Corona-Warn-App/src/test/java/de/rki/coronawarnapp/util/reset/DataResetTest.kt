package de.rki.coronawarnapp.util.reset

import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DataResetTest : BaseTest() {

    private fun createResettableSet() = DaggerResetTestComponent.create().resettableSet

    @Test
    fun `resets ever resettable`() = runBlockingTest {
        val resettableSet = createResettableSet()
        DataReset(resettableDataProvider = { resettableSet }).clearAllLocalData()

        coVerify {
            resettableSet.forEach { it.reset() }
        }
    }

    @Test
    fun `errors do not interrupt resetting`() = runBlockingTest {
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
