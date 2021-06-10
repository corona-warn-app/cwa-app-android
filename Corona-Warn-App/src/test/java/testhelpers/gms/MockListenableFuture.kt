package testhelpers.gms

import com.google.common.util.concurrent.ListenableFuture
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.Executor

object MockListenableFuture {
    fun <T> forResult(result: T) = mockk<ListenableFuture<T>>().apply {
        every { isDone } returns true
        every { get() } returns result
        every { get(any(), any()) } returns result
        every { isCancelled } returns false
        every { cancel(any()) } returns true
        every { addListener(any(), any()) } answers {
            val listener = arg<Runnable>(0)
            val executor = arg<Executor>(1)
            listener.run()
        }
    }
}
