package de.rki.coronawarnapp.covidcertificate.revocation.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import de.rki.coronawarnapp.util.HashExtensions.sha256
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.FakeDataStore
import java.io.IOException

class DccRevocationRepositoryTest : BaseTest() {

    private lateinit var dataStore: DataStore<Preferences>
    private val objectMapper = SerializationModule().jacksonObjectMapper()

    private val cachedRevocationChunk = CachedRevocationChunk(
        coordinates = RevocationEntryCoordinates(
            kid = "kid".sha256(),
            type = RevocationHashType.SIGNATURE,
            x = "x".sha256(),
            y = "y".sha256()
        ),
        revocationChunk = RevocationChunk(hashes = listOf("hashes".sha256()))
    )

    private fun createInstance(
        scope: CoroutineScope,
        data: DataStore<Preferences> = dataStore
    ): DccRevocationRepository = DccRevocationRepository(
        appScope = scope,
        objectMapper = objectMapper,
        dataStore = data
    )

    @BeforeEach
    fun setup() {
        dataStore = FakeDataStore()
    }

    @Test
    fun `saves revocation list as json`() = runTest2(ignoreActive = true) {
        val revocationListJson = """
            [
              {
                "coordinates": {
                  "kid": "XHfX/Y9R7QwqkT5GMm/20uoulcjs8WZ8cutyzMP8K0Y\u003d",
                  "type": "SIGNATURE",
                  "x": "LXEWQrcmsEQBYnyp+6wy9chTD7GQPMTbAiWHF5IaSIE\u003d",
                  "y": "ofzkNjhU/4iM/0uOeHXWAMJoI5BBKoz3mzfQsRFIsPo\u003d"
                },
                "revocationChunk": {
                  "hashes": [
                    "OkJ4+D7Oo4FfBo/34BTdZxvH15BmHxOTFbKVKIg1anI\u003d"
                  ]
                }
              }
            ]
        """.trimIndent()

        with(createInstance(scope = this)) {
            revocationList.first() shouldBe emptyList()
            saveCachedRevocationChunks(listOf(cachedRevocationChunk))
            revocationList.first().first() shouldBe cachedRevocationChunk

            dataStore.data
                .map { prefs -> prefs[CACHED_REVOCATION_CHUNKS_KEY] }
                .first()!!.toComparableJsonPretty() shouldBe revocationListJson

            saveCachedRevocationChunks(emptyList())
            revocationList.first() shouldBe emptyList()

            dataStore.data
                .map { prefs -> prefs[CACHED_REVOCATION_CHUNKS_KEY] }
                .first()!!.toComparableJsonPretty() shouldBe "[]"
        }
    }

    @Test
    fun `clear removes revocation list`() = runTest2(ignoreActive = true) {
        with(createInstance(scope = this)) {
            saveCachedRevocationChunks(listOf(cachedRevocationChunk))
            revocationList.first() shouldBe listOf(cachedRevocationChunk)
            dataStore.data
                .map { prefs -> prefs[CACHED_REVOCATION_CHUNKS_KEY] }
                .first() shouldNotBe null

            clear()
            revocationList.first() shouldBe emptyList()
            dataStore.data
                .map { prefs -> prefs[CACHED_REVOCATION_CHUNKS_KEY] }
                .first() shouldBe null
        }
    }

    @Test
    fun `clear does not throw`() = runTest2(ignoreActive = true) {
        val mockDataStore = mockk<DataStore<Preferences>> {
            every { data } returns flowOf()
            coEvery { updateData(any()) } throws IOException("Test error")
        }

        shouldNotThrowAny { createInstance(scope = this, data = mockDataStore).clear() }
    }
}
