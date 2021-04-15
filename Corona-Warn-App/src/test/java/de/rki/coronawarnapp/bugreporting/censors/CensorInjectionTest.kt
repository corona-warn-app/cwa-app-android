package de.rki.coronawarnapp.bugreporting.censors

import dagger.Component
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.BugReportingSharedModule
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.submission.SubmissionSettings
import io.github.classgraph.ClassGraph
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import javax.inject.Singleton

class CensorInjectionTest : BaseTest() {

    /**
     * Scan our class graph, and compare it against what we inject via Dagger.
     * Catch accidentally forgetting to add a bug censor.
     */
    @Test
    fun `all censors are injected`() {
        val component = DaggerBugCensorTestComponent.factory().create()
        val bugCensors = component.bugCensors

        Timber.v("We know %d censors.", bugCensors.size)
        require(bugCensors.isNotEmpty())

        val scanResult = ClassGraph()
            .acceptPackages("de.rki.coronawarnapp")
            .enableClassInfo()
            .scan()

        val ourCensors = scanResult
            .getClassesImplementing("de.rki.coronawarnapp.bugreporting.censors.BugCensor")

        Timber.v("Our project contains %d censor classes.", ourCensors.size)

        val injectedCensors = bugCensors.map { it.javaClass.name }.toSet()
        val existingCensors = ourCensors.map { it.name }.toSet()
        existingCensors.isEmpty() shouldBe false
        injectedCensors shouldContainAll existingCensors
    }
}

@Singleton
@Component(modules = [MockProvider::class, BugReportingSharedModule::class])
interface BugCensorTestComponent {

    val bugCensors: Set<BugCensor>

    @Component.Factory
    interface Factory {
        fun create(): BugCensorTestComponent
    }
}

@Module
class MockProvider {

    @Singleton
    @Provides
    fun diary(): ContactDiaryRepository = mockk()

    @Singleton
    @Provides
    fun submissionSettings(): SubmissionSettings = mockk()

    @Singleton
    @Provides
    fun coronaTestRepository(): CoronaTestRepository = mockk()
}
