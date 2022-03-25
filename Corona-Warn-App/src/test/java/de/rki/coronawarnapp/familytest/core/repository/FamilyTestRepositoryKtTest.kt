package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class FamilyTestRepositoryKtTest : BaseTest() {

    @Test
    fun `Validate testHasResultInterestingChange`() {
        testHasInterestingResultChange(
            oldState = CoronaTest.State.INVALID,
            newState = CoronaTest.State.INVALID
        ) shouldBe false

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
            oldState = CoronaTest.State.REDEEMED,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.POSITIVE
        ) shouldBe true

        testHasInterestingResultChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.NEGATIVE
        ) shouldBe true

        testHasInterestingResultChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.INVALID
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

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
            oldState = CoronaTest.State.POSITIVE,
            newState = CoronaTest.State.INVALID
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.NEGATIVE,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.POSITIVE,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasInterestingResultChange(
            oldState = CoronaTest.State.NEGATIVE,
            newState = CoronaTest.State.INVALID
        ) shouldBe false
    }
}
