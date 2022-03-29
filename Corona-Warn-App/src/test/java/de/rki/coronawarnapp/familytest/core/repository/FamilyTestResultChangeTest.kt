package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.hasChanged
import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class FamilyTestResultChangeTest : BaseTest() {

    @Test
    fun `testHasResultInterestingChange - SAME`() {
        Pair(
            CoronaTest.State.PENDING,
            CoronaTest.State.PENDING
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.NEGATIVE,
            CoronaTest.State.NEGATIVE
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.POSITIVE,
            CoronaTest.State.POSITIVE
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.RECYCLED,
            CoronaTest.State.RECYCLED
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.REDEEMED,
            CoronaTest.State.REDEEMED
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.INVALID,
            CoronaTest.State.INVALID
        ).hasChanged shouldBe false
    }

    @Test
    fun `testHasResultInterestingChange - RECYCLED`() {
        Pair(
            CoronaTest.State.PENDING,
            CoronaTest.State.RECYCLED
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.NEGATIVE,
            CoronaTest.State.RECYCLED
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.POSITIVE,
            CoronaTest.State.RECYCLED
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.INVALID,
            CoronaTest.State.RECYCLED
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.REDEEMED,
            CoronaTest.State.RECYCLED
        ).hasChanged shouldBe false
    }

    @Test
    fun `testHasResultInterestingChange - REDEEMED`() {
        Pair(
            CoronaTest.State.NEGATIVE,
            CoronaTest.State.REDEEMED
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.PENDING,
            CoronaTest.State.REDEEMED
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.POSITIVE,
            CoronaTest.State.REDEEMED
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.INVALID,
            CoronaTest.State.REDEEMED
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.RECYCLED,
            CoronaTest.State.REDEEMED
        ).hasChanged shouldBe false
    }

    @Test
    fun `testHasResultInterestingChange - INVALID`() {
        Pair(
            CoronaTest.State.PENDING,
            CoronaTest.State.INVALID
        ).hasChanged shouldBe true

        Pair(
            CoronaTest.State.INVALID,
            CoronaTest.State.PENDING
        ).hasChanged shouldBe false

        Pair(
            CoronaTest.State.NEGATIVE,
            CoronaTest.State.INVALID
        ).hasChanged shouldBe true

        Pair(
            CoronaTest.State.POSITIVE,
            CoronaTest.State.INVALID
        ).hasChanged shouldBe true

        Pair(
            CoronaTest.State.REDEEMED,
            CoronaTest.State.INVALID
        ).hasChanged shouldBe true
    }

    @Test
    fun `testHasResultInterestingChange - POSITIVE`() {
        Pair(
            CoronaTest.State.PENDING,
            CoronaTest.State.POSITIVE
        ).hasChanged shouldBe true

        Pair(
            CoronaTest.State.INVALID,
            CoronaTest.State.POSITIVE
        ).hasChanged shouldBe true
    }

    @Test
    fun `testHasResultInterestingChange - NEGATIVE`() {
        Pair(
            CoronaTest.State.PENDING,
            CoronaTest.State.NEGATIVE
        ).hasChanged shouldBe true

        Pair(
            CoronaTest.State.INVALID,
            CoronaTest.State.NEGATIVE
        ).hasChanged shouldBe true
    }
}
