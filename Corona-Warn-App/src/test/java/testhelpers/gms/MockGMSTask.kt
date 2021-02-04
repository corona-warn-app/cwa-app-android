package testhelpers.gms

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk
import timber.log.Timber

object MockGMSTask {
    fun <T> forError(error: Exception): Task<T> = mockk<Task<T>>().apply {
        every { addOnFailureListener(any()) } answers {
            val listener = arg<OnFailureListener>(0)
            listener.onFailure(error)
            this@apply
        }
        every { addOnSuccessListener(any()) } returns this
    }

    fun <T> forValue(value: T): Task<T> = mockk<Task<T>>().apply {
        every { addOnSuccessListener(any()) } answers {
            val listener = arg<OnSuccessListener<T>>(0)
            listener.onSuccess(value)
            this@apply
        }
        every { addOnFailureListener(any()) } returns this
    }

    fun <T> timeout(): Task<T> = mockk<Task<T>>().apply {
        every { addOnSuccessListener(any()) } answers {
            val listener = arg<OnSuccessListener<T>>(0)
            Timber.v("Listener set, but we will time it out: %s", listener)
            this@apply
        }
        every { addOnFailureListener(any()) } returns this
    }
}
