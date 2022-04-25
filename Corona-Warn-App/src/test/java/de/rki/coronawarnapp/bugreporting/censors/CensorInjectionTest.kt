package de.rki.coronawarnapp.bugreporting.censors

import dagger.Component
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.BugReportingSharedModule
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.profile.storage.ProfileRepository
import de.rki.coronawarnapp.profile.storage.ProfileSettingsDataStore
import de.rki.coronawarnapp.submission.SubmissionSettings
import io.github.classgraph.ClassGraph
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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
    fun diary(): ContactDiaryRepository = mockk {
        every { people } returns flowOf(emptyList())
        every { personEncounters } returns flowOf(emptyList())
        every { locations } returns flowOf(emptyList())
        every { locationVisits } returns flowOf(emptyList())
        every { testResults } returns flowOf(emptyList())
    }

    @Singleton
    @Provides
    fun submissionSettings(): SubmissionSettings = mockk()

    @Singleton
    @Provides
    fun coronaTestRepository(): CoronaTestRepository = mockk {
        every { coronaTests } returns flowOf(emptySet())
        every { allCoronaTests } returns flowOf(emptySet())
        every { personalTestsInRecycleBin } returns flowOf(emptySet())
    }

    @Singleton
    @Provides
    fun coronaTestCertificateRepository(): TestCertificateRepository = mockk {
        every { certificates } returns flowOf(emptySet())
    }

    @Singleton
    @Provides
    fun checkInRepository(): CheckInRepository = mockk {
        every { allCheckIns } returns flowOf(emptyList())
    }

    @Singleton
    @Provides
    fun traceLocationRepository(): TraceLocationRepository = mockk {
        every { allTraceLocations } returns flowOf(emptyList())
    }

    @Singleton
    @Provides
    fun ratProfileSettings(): ProfileSettingsDataStore = mockk()

    @Singleton
    @Provides
    fun personCertificatesSettings(): PersonCertificatesSettings = mockk {
        every { currentCwaUser } returns flowOf(null)
    }

    @Singleton
    @Provides
    fun familyTestRepository(): FamilyTestRepository = mockk {
        every { familyTests } returns flowOf(emptySet())
        every { familyTestsInRecycleBin } returns flowOf(emptySet())
    }

    @Singleton
    @Provides
    fun profileRepository(): ProfileRepository = mockk {
        every { profilesFlow } returns flowOf(emptySet())
    }
}
