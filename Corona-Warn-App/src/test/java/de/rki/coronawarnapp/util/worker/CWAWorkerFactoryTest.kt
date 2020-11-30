package de.rki.coronawarnapp.util.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.impl.workers.DiagnosticsWorker
import de.rki.coronawarnapp.worker.DiagnosisKeyRetrievalOneTimeWorker
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Provider

class CWAWorkerFactoryTest : BaseTest() {

    private val workerFactories =
        mutableMapOf<Class<out ListenableWorker>, Provider<InjectedWorkerFactory<out ListenableWorker>>>()

    @MockK lateinit var context: Context
    @MockK lateinit var workerParameters: WorkerParameters
    @MockK lateinit var ourWorker: DiagnosisKeyRetrievalOneTimeWorker
    @MockK lateinit var ourFactory: DiagnosisKeyRetrievalOneTimeWorker.Factory

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { ourFactory.create(context, workerParameters) } returns ourWorker
        workerFactories[DiagnosisKeyRetrievalOneTimeWorker::class.java] = Provider { ourFactory }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    fun createInstance() = CWAWorkerFactory(
        workerFactories
    )

    @Test
    fun `instantiate one of our workers`() {
        val instance = createInstance()
        instance.createWorker(
            context, DiagnosisKeyRetrievalOneTimeWorker::class.qualifiedName!!, workerParameters
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
}


