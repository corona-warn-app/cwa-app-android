package de.rki.coronawarnapp.util.collections

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CollectionsExtensionTest {

    private val numbers = listOf(
        "1",
        "2",
        "3",
        "Not a number",
        "3",
        "2",
        "1",
        "Not a number",
        "1",
        "Not a number",
        "2",
        "Not a number",
        "3",
        "Not a number",
    )

    @Test
    fun `group by not null`() {
        numbers.groupByNotNull { it.toIntOrNull() }.also {
            it.keys shouldBe setOf(1, 2, 3)

            it.values shouldBe listOf(
                listOf("1", "1", "1"),
                listOf("2", "2", "2"),
                listOf("3", "3", "3"),
            )
        }
    }
}
