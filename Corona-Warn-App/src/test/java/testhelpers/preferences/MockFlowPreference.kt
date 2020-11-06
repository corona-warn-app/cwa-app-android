package testhelpers.preferences

import de.rki.coronawarnapp.util.preferences.FlowPreference
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

fun <T> mockFlowPreference(
    defaultValue: T
): FlowPreference<T> {
    val instance = mockk<FlowPreference<T>>()
    val flow = MutableStateFlow(defaultValue)
    every { instance.flow } returns flow
    every { instance.value } returns flow.value
    every { instance.update(any()) } answers {
        val updateCall = arg<(T) -> T>(0)
        flow.value = updateCall(flow.value)
    }
    return instance
}
