package de.rki.coronawarnapp.util.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.workers.DiagnosticsWorker
import de.rki.coronawarnapp.diagnosiskeys.execution.DiagnosisKeyRetrievalWorker
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Provider

class CWAWorkerFactoryTest : BaseTest() {

    private val workerFactories =
        mutableMapOf<Class<out ListenableWorker>, Provider<InjectedWorkerFactory<out ListenableWorker>>>()

    @MockK lateinit var context: Context
    @MockK lateinit var workerParameters: WorkerParameters
    @MockK lateinit var ourWorker: DiagnosisKeyRetrievalWorker
    @MockK lateinit var ourFactory: DiagnosisKeyRetrievalWorker.Factory

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { ourFactory.create(context, workerParameters) } returns ourWorker
        workerFactories[DiagnosisKeyRetrievalWorker::class.java] = Provider { ourFactory }
    }

    fun createInstance() = CWAWorkerFactory(
        workerFactories
    )

    @Test
    fun `instantiate one of our workers`() {
        val instance = createInstance()
        instance.createWorker(
            context,
            DiagnosisKeyRetrievalWorker::class.qualifiedName!!,
            workerParameters
        ) shouldBe ourWorker
    }

    @Test
    fun `instantiate an unknown worker`() {
        val instance = createInstance()
        val worker1 = instance.createWorker(context, DiagnosticsWorker::class.qualifiedName!!, workerParameters)
        worker1 shouldNotBe null
        val worker2 = instance.createWorker(context, DiagnosticsWorker::class.qualifiedName!!, workerParameters)
        worker1 shouldNotBe worker2
    }

    /**
     * Workers are initialized based on their class name.
     * That class name is stored as part of the worker arguments.
     * If we refactor a worker, the previously used classname will be unknown.
     * Returning null will cause the periodic worker to be dequeued.
     */
    @Test
    fun `class names that can not be instantiated are treated like an unknown worker`() {
        val instance = createInstance()
        instance.createWorker(
            context,
            "abc.im.a.ghost.def",
            workerParameters
        ) shouldBe null
    }
}
