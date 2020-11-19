package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CountryDataTest : BaseTest() {
    private val locationCode = LocationCode("DE")

    private fun createCachedKey(dayString: String, hourString: String? = null): CachedKey {
        return mockk<CachedKey>().apply {
            every { info } returns mockk<CachedKeyInfo>().apply {
                every { location } returns locationCode
                every { day } returns LocalDate.parse(dayString)
                every { hour } returns hourString?.let { LocalTime.parse(it) }
            }
        }
    }

    @Test
    fun `missing days default`() {
        val availableDates = listOf(
            "2222-12-30", "2222-12-31"
        ).map { LocalDate.parse(it) }
        val cd = LocationDays(locationCode, availableDates)

        cd.dayData shouldBe availableDates

        val cachedDays = listOf(
            createCachedKey("2222-12-30")
        )

        cd.getMissingDays(cachedDays) shouldBe listOf(availableDates[1])
        cd.toMissingDays(cachedDays) shouldBe cd.copy(
            dayData = listOf(availableDates[1])
        )
    }

    @Test
    fun `missing days empty day data`() {
        val availableDates = emptyList<LocalDate>()
        val cd = LocationDays(locationCode, availableDates)

        cd.dayData shouldBe availableDates

        val cachedDays = listOf(
            createCachedKey("2222-12-30"),
            createCachedKey("2222-12-31")
        )

        cd.getMissingDays(cachedDays) shouldBe emptyList()
        cd.toMissingDays(cachedDays) shouldBe null
    }

    @Test
    fun `missing days empty cache`() {
        val availableDates = listOf(
            "2222-11-28", "2222-11-29"
        ).map { LocalDate.parse(it) }
        val cd = LocationDays(locationCode, availableDates)

        cd.dayData shouldBe availableDates

        val cachedDays = emptyList<CachedKey>()

        cd.getMissingDays(cachedDays) shouldBe availableDates
        cd.toMissingDays(cachedDays) shouldBe cd
    }

    @Test
    fun `missing days disjunct`() {
        val availableDates = listOf(
            "2222-11-28", "2222-11-29"
        ).map { LocalDate.parse(it) }
        val cd = LocationDays(locationCode, availableDates)

        cd.dayData shouldBe availableDates

        val cachedDays = listOf(
            createCachedKey("2222-12-28"),
            createCachedKey("2222-12-29")
        )

        cd.getMissingDays(cachedDays) shouldBe availableDates
        cd.toMissingDays(cachedDays) shouldBe cd
    }

    @Test
    fun `missing days none missing`() {
        val availableDates = listOf(
            "2222-12-30", "2222-12-31"
        ).map { LocalDate.parse(it) }
        val cd = LocationDays(locationCode, availableDates)

        cd.dayData shouldBe availableDates

        val cachedDays = listOf(
            createCachedKey("2222-12-30"),
            createCachedKey("2222-12-31")
        )

        cd.getMissingDays(cachedDays) shouldBe emptyList()
        cd.toMissingDays(cachedDays) shouldBe null
    }

    @Test
    fun `missing hours default`() {
        val availableHours = mapOf(
            LocalDate.parse("2222-12-30") to listOf(
                LocalTime.parse("19:00"), LocalTime.parse("20:00")
            ),
            LocalDate.parse("2222-12-31") to listOf(
                LocalTime.parse("22:00"), LocalTime.parse("23:00")
            )
        )
        val cd = LocationHours(locationCode, availableHours)

        cd.hourData shouldBe availableHours

        val cachedHours = listOf(
            createCachedKey("2222-12-30", "19:00"),
            createCachedKey("2222-12-31", "23:00")
        )

        val missingHours = mapOf(
            LocalDate.parse("2222-12-30") to listOf(LocalTime.parse("20:00")),
            LocalDate.parse("2222-12-31") to listOf(LocalTime.parse("22:00"))
        )

        cd.getMissingHours(cachedHours) shouldBe missingHours
        cd.toMissingHours(cachedHours) shouldBe cd.copy(hourData = missingHours)
    }

    @Test
    fun `missing hours empty available hour data`() {
        val availableHours: Map<LocalDate, List<LocalTime>> = emptyMap()
        val cd = LocationHours(locationCode, availableHours)

        cd.hourData shouldBe availableHours

        val cachedHours = listOf(
            createCachedKey("2222-12-30", "19:00"),
            createCachedKey("2222-12-31", "23:00")
        )

        cd.getMissingHours(cachedHours) shouldBe emptyMap()
        cd.toMissingHours(cachedHours) shouldBe null
    }

    @Test
    fun `missing hours faulty hour map`() {
        val availableHours = mapOf(
            LocalDate.parse("2222-12-30") to emptyList<LocalTime>()
        )
        val cd = LocationHours(locationCode, availableHours)

        cd.hourData shouldBe availableHours

        val cachedHours = listOf(
            createCachedKey("2222-12-30", "19:00"),
            createCachedKey("2222-12-31", "23:00")
        )

        cd.getMissingHours(cachedHours) shouldBe emptyMap()
        cd.toMissingHours(cachedHours) shouldBe null
    }

    @Test
    fun `missing hours empty cache`() {
        val availableHours = mapOf(
            LocalDate.parse("2222-12-30") to listOf(
                LocalTime.parse("19:00"), LocalTime.parse("20:00")
            ),
            LocalDate.parse("2222-12-31") to listOf(
                LocalTime.parse("22:00"), LocalTime.parse("23:00")
            )
        )
        val cd = LocationHours(locationCode, availableHours)

        cd.hourData shouldBe availableHours

        val cachedHours = emptyList<CachedKey>()

        cd.getMissingHours(cachedHours) shouldBe availableHours
        cd.toMissingHours(cachedHours) shouldBe cd.copy(hourData = availableHours)
    }

    @Test
    fun `missing hours disjunct`() {
        val availableHours = mapOf(
            LocalDate.parse("2222-12-30") to listOf(
                LocalTime.parse("19:00"), LocalTime.parse("20:00")
            ),
            LocalDate.parse("2222-12-31") to listOf(
                LocalTime.parse("22:00"), LocalTime.parse("23:00")
            )
        )
        val cd = LocationHours(locationCode, availableHours)

        cd.hourData shouldBe availableHours

        val cachedHours = listOf(
            createCachedKey("2022-12-30", "19:00"),
            createCachedKey("2022-12-31", "23:00")
        )

        cd.getMissingHours(cachedHours) shouldBe availableHours
        cd.toMissingHours(cachedHours) shouldBe cd.copy(hourData = availableHours)
    }

    @Test
    fun `missing hours none missing`() {
        val availableHours = mapOf(
            LocalDate.parse("2222-12-30") to listOf(
                LocalTime.parse("19:00"), LocalTime.parse("20:00")
            ),
            LocalDate.parse("2222-12-31") to listOf(
                LocalTime.parse("22:00"), LocalTime.parse("23:00")
            )
        )
        val cd = LocationHours(locationCode, availableHours)

        cd.hourData shouldBe availableHours

        val cachedHours = listOf(
            createCachedKey("2222-12-30", "19:00"),
            createCachedKey("2222-12-30", "20:00"),
            createCachedKey("2222-12-31", "22:00"),
            createCachedKey("2222-12-31", "23:00")
        )

        cd.getMissingHours(cachedHours) shouldBe emptyMap()
        cd.toMissingHours(cachedHours) shouldBe null
    }

    @Test
    fun `calculate approximate required space for day data`() {
        LocationDays(LocationCode("DE"), emptyList()).approximateSizeInBytes shouldBe 0
        LocationDays(
            LocationCode("DE"),
            listOf(LocalDate.parse("2222-12-30"))
        ).approximateSizeInBytes shouldBe 512 * 1024L
        LocationDays(
            LocationCode("DE"),
            listOf(LocalDate.parse("2222-12-30"), LocalDate.parse("2222-12-31"))
        ).approximateSizeInBytes shouldBe 2 * 512 * 1024L
    }

    @Test
    fun `calculate approximate required space for day hour`() {
        LocationHours(LocationCode("DE"), emptyMap()).approximateSizeInBytes shouldBe 0
        LocationHours(
            LocationCode("DE"),
            mapOf(LocalDate.parse("2222-12-30") to listOf(LocalTime.parse("23:00")))
        ).approximateSizeInBytes shouldBe 22 * 1024L
        LocationHours(
            LocationCode("DE"),
            mapOf(
                LocalDate.parse("2222-12-30") to listOf(LocalTime.parse("23:00")),
                LocalDate.parse("2222-12-31") to listOf(
                    LocalTime.parse("22:00"),
                    LocalTime.parse("23:00")
                )

            )
        ).approximateSizeInBytes shouldBe 3 * 22 * 1024L
    }
}
