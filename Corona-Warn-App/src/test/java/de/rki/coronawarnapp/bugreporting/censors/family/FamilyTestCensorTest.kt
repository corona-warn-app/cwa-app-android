package de.rki.coronawarnapp.bugreporting.censors.family

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runTest
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength")
internal class FamilyTestCensorTest {

    private val test = CoronaTest(
        type = BaseCoronaTest.Type.RAPID_ANTIGEN,
        identifier = "qrcode-RAPID_ANTIGEN-6a90a60",
        registeredAt = Instant.parse("2022-04-13T15:33:14.637Z"),
        registrationToken = "ac7f72a0-0135-41c5-ad14-6a4299865aca",
        testResult = CoronaTestResult.PCR_OR_RAT_PENDING,
        additionalInfo = CoronaTest.AdditionalInfo(
            firstName = "Louisa",
            lastName = "Davis",
            dateOfBirth = LocalDate.parse("1999-12-02"),
            createdAt = Instant.parse("2022-04-13T15:33:14.637Z")
        )
    )

    private val test2 = CoronaTest(
        type = BaseCoronaTest.Type.PCR,
        identifier = "qrcode-PCR-12345",
        registeredAt = Instant.parse("2022-04-13T15:33:14.637Z"),
        registrationToken = "regToken1234",
        testResult = CoronaTestResult.PCR_OR_RAT_PENDING,
    )

    private val familyTest = FamilyCoronaTest(
        personName = "Angelina",
        test
    )

    private val familyTestRecycleBin = FamilyCoronaTest(
        personName = "Maria",
        test2
    )

    @MockK lateinit var familyTestRepository: FamilyTestRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { familyTestRepository.familyTests } returns flowOf(setOf(familyTest))
        coEvery { familyTestRepository.familyTestsInRecycleBin } returns flowOf(setOf(familyTestRecycleBin))
    }

    private fun createInstance() = FamilyTestCensor(
        TestCoroutineScope(),
        familyTestRepository,
    )

    @Test
    fun `checkLog() should return censored LogLine`() = runTest {

        val censor = createInstance()

        val logLineToCensor =
            "[FamilyCoronaTest(personName=Angelina, coronaTest=CoronaTest(identifier=qrcode-RAPID_ANTIGEN-6a90a60, type=RAPID_ANTIGEN, registeredAt=2022-04-13T15:33:14.637Z, registrationToken=ac7f72a0-0135-41c5-ad14-6a4299865aca, testResult=RAT_NEGATIVE(6), labId=null, qrCodeHash=795d7c5c3fff50d58694fbd3e1a91d2e1f88f2638a3031c5a3ac21ddea986cf9, dcc=Dcc(isDccSupportedByPoc=false, isDccConsentGiven=true, isDccDataSetCreated=false), uiState=UiState(isViewed=true, didShowBadge=true, isResultAvailableNotificationSent=false, hasResultChangeBadge=false), additionalInfo=AdditionalInfo(createdAt=2022-04-13T11:06:29.000Z, firstName=Louisa, lastName=Davis, dateOfBirth=1999-12-02, sampleCollectedAt=2022-04-13T06:00:00.000Z), recycledAt=null)"

        val censored = censor.checkLog(logLineToCensor)!!
            .compile()!!.censored

        // personName
        censored.contains("Angelina") shouldBe false
        // identifier
        censored.contains("qrcode-RAPID_ANTIGEN-6a90a60") shouldBe false
        // registration token
        censored.contains("ac7f72a0-0135-41c5-ad14-6a4299865aca") shouldBe false
        // first name
        censored.contains("Louisa") shouldBe false
        // last name
        censored.contains("Davis") shouldBe false
        // date of birth
        censored.contains("1999-12-02") shouldBe false
    }

    @Test
    fun `checkLog() should return null if no data to censor was set`() = runTest {
        coEvery { familyTestRepository.familyTests } returns flowOf(setOf())
        coEvery { familyTestRepository.familyTestsInRecycleBin } returns flowOf(setOf())
        val censor = createInstance()

        val logLineNotToCensor = "Nothing to censor here, Angelina"

        censor.checkLog(logLineNotToCensor) shouldBe null
    }

    @Test
    fun `personName of family should be censored`() = runTest {
        coEvery { familyTestRepository.familyTests } returns flowOf(setOf())
        coEvery { familyTestRepository.familyTestsInRecycleBin } returns flowOf(setOf())
        val censor = createInstance()
        censor.addName("Angelina")

        val logLineNotToCensor = "Please censor, Angelina"

        censor.checkLog(logLineNotToCensor)!!.compile()!!.censored shouldBe "Please censor, #name"
    }

    @Test
    fun `checkLog() should return null if nothing should be censored`() = runTest {
        val censor = createInstance()
        val logLineNotToCensor = "Nothing to censor here"

        censor.checkLog(logLineNotToCensor) shouldBe null
    }
}
