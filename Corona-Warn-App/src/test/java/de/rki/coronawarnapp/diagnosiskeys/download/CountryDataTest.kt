package de.rki.coronawarnapp.diagnosiskeys.download

import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CountryDataTest : BaseTest() {


    //    @Test
//    fun testGetMissingDaysFromDiff() {
//        val c1 = KeyCacheEntity()
//        c1.id = "10008bf0-8890-356d-a4a4-dc375553160a"
//        c1.path =
//            "/data/user/0/de.rki.coronawarnapp.dev/cache/key-export/10008bf0-8890-356d-a4a4-dc375553160a.zip"
//        c1.type = KeyCacheRepository.DateEntryType.DAY.ordinal
//
//        val c2 = KeyCacheEntity()
//        c2.id = "a8cc7b31-843e-3924-b918-023c386aec69"
//        c2.path =
//            "/data/user/0/de.rki.coronawarnapp.dev/cache/key-export/a8cc7b31-843e-3924-b918-023c386aec69.zip"
//        c2.type = KeyCacheRepository.DateEntryType.DAY.ordinal
//
//        val cacheEntries: Collection<KeyCacheEntity> = listOf(c1, c2)
//
//        val countryDataWrapper =
//            CountryDataWrapper("DE", listOf("2020-08-29", "2020-08-26", "2020-08-28"))
//
//        val result = countryDataWrapper.getMissingDates(cacheEntries)
//
//        result.size shouldBe 1
//        result.elementAt(0) shouldBe "2020-08-28"
//    }

    @Test
    fun `missing hours default`() {
        TODO()
    }

    @Test
    fun `missing hours empty`() {
        TODO()
    }

    @Test
    fun `missing hours disjunct`() {
        TODO()
    }

    @Test
    fun `missing hours none missing`() {
        TODO()
    }

    @Test
    fun `missing days default`() {
        TODO()
    }

    @Test
    fun `missing days empty`() {
        TODO()
    }

    @Test
    fun `missing days disjunct`() {
        TODO()
    }

    @Test
    fun `missing days none missing`() {
        TODO()
    }

}
