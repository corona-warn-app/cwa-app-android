package de.rki.coronawarnapp.update

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun testVersionMajorOlder() {
        val result = VersionComparator.isVersionOlder(1000000, 2000000)
        assertThat(result, `is`(true))
    }

    @Test
    fun testVersionMinorOlder() {
        val result = VersionComparator.isVersionOlder(1000000, 1010000)
        assertThat(result, `is`(true))
    }

    @Test
    fun testVersionPatchOlder() {
        val result = VersionComparator.isVersionOlder(1000100, 1000200)
        assertThat(result, `is`(true))
    }

    @Test
    fun testVersionMajorNewer() {
        val result = VersionComparator.isVersionOlder(2000000, 1000000)
        assertThat(result, `is`(false))
    }

    @Test
    fun testVersionMinorNewer() {
        val result = VersionComparator.isVersionOlder(1020000, 1010000)
        assertThat(result, `is`(false))
    }

    @Test
    fun testVersionPatchNewer() {
        val result = VersionComparator.isVersionOlder(1000300, 1000200)
        assertThat(result, `is`(false))
    }

    @Test
    fun testSameVersion() {
        val result = VersionComparator.isVersionOlder(1000100, 1000100)
        assertThat(result, `is`(false))
    }

    @Test
    fun testIfMajorIsNewerButMinorSmallerNumber() {
        val result = VersionComparator.isVersionOlder(3010000, 1020000)
        assertThat(result, `is`(false))
    }

    @Test
    fun testIfMinorIsNewerButPatchSmallerNumber() {
        val result = VersionComparator.isVersionOlder(1030100, 1020400)
        assertThat(result, `is`(false))
    }
}
