package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class FamilyTestResultChangeTest : BaseTest() {

    @Test
    fun `testHasResultInterestingChange - SAME`() {
        testHasInterestingResultChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.PENDING
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.NEGATIVE,
            newState = CoronaTest.State.NEGATIVE
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.POSITIVE,
            newState = CoronaTest.State.POSITIVE
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.RECYCLED,
            newState = CoronaTest.State.RECYCLED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.REDEEMED,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.INVALID,
            newState = CoronaTest.State.INVALID
        ) shouldBe false
    }

    @Test
    fun `testHasResultInterestingChange - RECYCLED`() {
        testHasInterestingResultChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.RECYCLED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.NEGATIVE,
            newState = CoronaTest.State.RECYCLED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.POSITIVE,
            newState = CoronaTest.State.RECYCLED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.INVALID,
            newState = CoronaTest.State.RECYCLED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.REDEEMED,
            newState = CoronaTest.State.RECYCLED
        ) shouldBe false
    }

    @Test
    fun `testHasResultInterestingChange - REDEEMED`() {
        testHasInterestingResultChange(
            oldState = CoronaTest.State.NEGATIVE,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.POSITIVE,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.INVALID,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.RECYCLED,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false
    }

    @Test
    fun `testHasResultInterestingChange - INVALID`() {
        testHasInterestingResultChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.INVALID
        ) shouldBe true

        testHasInterestingResultChange(
            oldState = CoronaTest.State.INVALID,
            newState = CoronaTest.State.PENDING
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.NEGATIVE,
            newState = CoronaTest.State.INVALID
        ) shouldBe true

        testHasInterestingResultChange(
            oldState = CoronaTest.State.POSITIVE,
            newState = CoronaTest.State.INVALID
        ) shouldBe true

        testHasInterestingResultChange(
            oldState = CoronaTest.State.REDEEMED,
            newState = CoronaTest.State.INVALID
        ) shouldBe true
    }

    @Test
    fun `testHasResultInterestingChange - POSITIVE`() {
        testHasInterestingResultChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.POSITIVE
        ) shouldBe true

        testHasInterestingResultChange(
            oldState = CoronaTest.State.INVALID,
            newState = CoronaTest.State.POSITIVE
        ) shouldBe true

        testHasInterestingResultChange(
            oldState = CoronaTest.State.RECYCLED,
            newState = CoronaTest.State.POSITIVE
        ) shouldBe false
    }

    @Test
    fun `testHasResultInterestingChange - NEGATIVE`() {
        testHasInterestingResultChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.NEGATIVE
        ) shouldBe true

        testHasInterestingResultChange(
            oldState = CoronaTest.State.INVALID,
            newState = CoronaTest.State.NEGATIVE
        ) shouldBe true

        testHasInterestingResultChange(
            oldState = CoronaTest.State.RECYCLED,
            newState = CoronaTest.State.NEGATIVE
        ) shouldBe false
    }
}
