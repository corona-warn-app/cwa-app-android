package de.rki.coronawarnapp.contactdiary.ui.exporter

import android.content.Context
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.util.ContactDiaryData
import de.rki.coronawarnapp.contactdiary.util.mockStringsForContactDiaryExporterTests
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class, CoroutinesTestExtension::class)
internal class ContactDiaryExporterTest {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var context: Context

    private val numberOfLastDaysToExport = 15

    @BeforeEach
    fun setUp() {

        // In these test, now = January, 15
        every { timeStamper.nowUTC } returns Instant.parse("2021-01-15T00:00:00.000Z")

        mockStringsForContactDiaryExporterTests(context)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createInstance() = ContactDiaryExporter(
        context,
        timeStamper,
        TestDispatcherProvider()
    )

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun `createExport() should produce correct export`(
        testItem: ExporterTestItem
    ) = runBlockingTest {

        createInstance().createExport(
            testItem.personEncounters,
            testItem.locationVisits,
            numberOfLastDaysToExport
        ) shouldBe testItem.expectedExport
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun provideArguments() = listOf(
            ExporterTestItem(
                personEncounters = emptyList(),
                locationVisits = emptyList(),
                expectedExport =
                    """
                    Kontakte der letzten 15 Tage (01.01.2021 - 15.01.2021)
                    Die nachfolgende Liste dient dem zuständigen Gesundheitsamt zur Kontaktnachverfolgung gem. § 25 IfSG.
                    
                    """.trimIndent()
            ),
            ExporterTestItem(
                personEncounters = ContactDiaryData.TWO_PERSONS_NO_ADDITIONAL_DATA,
                locationVisits = ContactDiaryData.TWO_LOCATIONS_NO_ADDITIONAL_DATA,
                expectedExport =
                    """
                    Kontakte der letzten 15 Tage (01.01.2021 - 15.01.2021)
                    Die nachfolgende Liste dient dem zuständigen Gesundheitsamt zur Kontaktnachverfolgung gem. § 25 IfSG.

                    02.01.2021 Constantin Frenzel
                    02.01.2021 Barber
                    01.01.2021 Andrea Steinhauer
                    01.01.2021 Bakery
                
                    """.trimIndent()
            ),
            ExporterTestItem(
                personEncounters = ContactDiaryData.TWO_PERSONS_WITH_PHONE_NUMBERS,
                locationVisits = ContactDiaryData.TWO_LOCATIONS_WITH_EMAIL,
                expectedExport =
                    """
                    Kontakte der letzten 15 Tage (01.01.2021 - 15.01.2021)
                    Die nachfolgende Liste dient dem zuständigen Gesundheitsamt zur Kontaktnachverfolgung gem. § 25 IfSG.

                    02.01.2021 Constantin Frenzel; Tel. +49 987 654321
                    02.01.2021 Barber; eMail barber@icutyourhair.com
                    01.01.2021 Andrea Steinhauer; Tel. +49 123 456789
                    01.01.2021 Bakery; eMail baker@ibakeyourbread.com
                
                    """.trimIndent()
            ),
            ExporterTestItem(
                personEncounters = ContactDiaryData.TWO_PERSONS_WITH_PHONE_NUMBERS_AND_EMAIL,
                locationVisits = ContactDiaryData.TWO_LOCATIONS_WITH_PHONE_NUMBERS_AND_EMAIL,
                expectedExport =
                    """
                    Kontakte der letzten 15 Tage (01.01.2021 - 15.01.2021)
                    Die nachfolgende Liste dient dem zuständigen Gesundheitsamt zur Kontaktnachverfolgung gem. § 25 IfSG.

                    02.01.2021 Constantin Frenzel; Tel. +49 987 654321; eMail constantin.frenzel@example.com
                    02.01.2021 Barber; Tel. +99 888 777777; eMail barber@icutyourhair.com
                    01.01.2021 Andrea Steinhauer; Tel. +49 123 456789; eMail andrea.steinhauer@example.com
                    01.01.2021 Bakery; Tel. +11 222 333333; eMail baker@ibakeyourbread.com
                
                    """.trimIndent()
            ),
            ExporterTestItem(
                personEncounters = ContactDiaryData.TWO_PERSONS_WITH_ATTRIBUTES,
                locationVisits = ContactDiaryData.TWO_LOCATIONS_WITH_DURATION,
                expectedExport =
                    """
                    Kontakte der letzten 15 Tage (01.01.2021 - 15.01.2021)
                    Die nachfolgende Liste dient dem zuständigen Gesundheitsamt zur Kontaktnachverfolgung gem. § 25 IfSG.

                    02.01.2021 Constantin Frenzel; Kontaktdauer > 15 Minuten; ohne Maske; im Gebäude
                    02.01.2021 Barber; Dauer 01:45 h
                    01.01.2021 Andrea Steinhauer; Kontaktdauer < 15 Minuten; mit Maske; im Freien
                    01.01.2021 Bakery; Dauer 00:15 h
                
                    """.trimIndent()
            ),
            ExporterTestItem(
                personEncounters = ContactDiaryData.TWO_PERSONS_WITH_CIRCUMSTANCES,
                locationVisits = ContactDiaryData.TWO_LOCATIONS_WITH_CIRCUMSTANCES,
                expectedExport =
                    """    
                    Kontakte der letzten 15 Tage (01.01.2021 - 15.01.2021)
                    Die nachfolgende Liste dient dem zuständigen Gesundheitsamt zur Kontaktnachverfolgung gem. § 25 IfSG.

                    02.01.2021 Constantin Frenzel; saßen nah beieinander
                    02.01.2021 Barber; Nobody was wearing a mask, but needed a haircut real bad
                    01.01.2021 Andrea Steinhauer; Sicherheitsmaßnahmen eingehalten
                    01.01.2021 Bakery; Very crowdy, but delicious bread
                
                    """.trimIndent()
            )
        ).map { testItem -> Arguments.of(testItem) }
    }

    data class ExporterTestItem(
        val personEncounters: List<ContactDiaryPersonEncounter>,
        val locationVisits: List<ContactDiaryLocationVisit>,
        val expectedExport: String
    )
}
