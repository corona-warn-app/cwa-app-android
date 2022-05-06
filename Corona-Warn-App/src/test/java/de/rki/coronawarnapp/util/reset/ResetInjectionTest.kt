package de.rki.coronawarnapp.util.reset

import dagger.Component
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.submission.SubmissionModule
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import io.github.classgraph.ClassGraph
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Singleton

class ResetInjectionTest : BaseTest() {

    @Test
    fun `all resettable are injected`() {
        val resettableSet = DaggerResetTestComponent.create().resettableSet

        println("We know ${resettableSet.size} resettable")
        resettableSet.isNotEmpty() shouldBe true

        val scanResult = ClassGraph()
            .acceptPackages("de.rki.coronawarnapp")
            .enableClassInfo()
            .scan()

        val resettableClasses = scanResult
            .getClassesImplementing(Resettable::class.java)
            .filterNot { it.isAbstract }

        println("Our project contains ${resettableClasses.size} resettable classes")
        val injected = resettableSet.map { it::class.java.simpleName }.toSet()
        val existing = resettableClasses.map { it.simpleName }.toSet()
        injected shouldContainAll existing
    }
}

@Singleton
@Component(modules = [MockProvider::class, SubmissionModule.BindsModule::class])
interface ResetTestComponent {

    val resettableSet: Set<Resettable>

    @Component.Factory
    interface Factory {
        fun create(): ResetTestComponent
    }
}

@Module
object MockProvider {

    @Provides
    fun submissionSettings(): SubmissionSettings = mockk()

    @Provides
    fun provideTEKHistoryStorage(): TEKHistoryStorage = mockk()
}
