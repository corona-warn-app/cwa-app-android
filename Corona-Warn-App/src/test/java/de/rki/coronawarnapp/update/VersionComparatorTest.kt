package de.rki.coronawarnapp.update

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun testVersionMajorOlder() {
        val result = VersionComparator.isVersionOlder("1.0.0", "2.0.0")
        assertThat(result, `is`(true))
    }

    @Test
    fun testVersionMinorOlder() {
        val result = VersionComparator.isVersionOlder("1.0.0", "1.1.0")
        assertThat(result, `is`(true))
    }

    @Test
    fun testVersionPatchOlder() {
        val result = VersionComparator.isVersionOlder("1.0.1", "1.0.2")
        assertThat(result, `is`(true))
    }

    @Test
    fun testVersionMajorNewer() {
        val result = VersionComparator.isVersionOlder("2.0.0", "1.0.0")
        assertThat(result, `is`(false))
    }

    @Test
    fun testVersionMinorNewer() {
        val result = VersionComparator.isVersionOlder("1.2.0", "1.1.0")
        assertThat(result, `is`(false))
    }

    @Test
    fun testVersionPatchNewer() {
        val result = VersionComparator.isVersionOlder("1.0.3", "1.0.2")
        assertThat(result, `is`(false))
    }

    @Test
    fun testSameVersion() {
        val result = VersionComparator.isVersionOlder("1.0.1", "1.0.1")
        assertThat(result, `is`(false))
    }

    @Test
    fun testIfMajorIsNewerButMinorSmallerNumber() {
        val result = VersionComparator.isVersionOlder("3.1.0", "1.2.0")
        assertThat(result, `is`(false))
    }

    @Test
    fun testIfMinorIsNewerButPatchSmallerNumber() {
        val result = VersionComparator.isVersionOlder("1.3.1", "1.2.4")
        assertThat(result, `is`(false))
    }
}
