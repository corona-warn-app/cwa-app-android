package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class FamilyTestRepositoryKtTest : BaseTest() {

    @Test
    fun `Validate testHasResultInterestingChange`() {
        testHasResultInterestingChange(
            oldState = CoronaTest.State.INVALID,
            newState = CoronaTest.State.INVALID
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.PENDING
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.NEGATIVE,
            newState = CoronaTest.State.NEGATIVE
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.POSITIVE,
            newState = CoronaTest.State.POSITIVE
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.RECYCLED,
            newState = CoronaTest.State.RECYCLED
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.REDEEMED,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.REDEEMED,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.POSITIVE
        ) shouldBe true

        testHasResultInterestingChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.NEGATIVE
        ) shouldBe true

        testHasResultInterestingChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.INVALID
        ) shouldBe true

        testHasResultInterestingChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.PENDING,
            newState = CoronaTest.State.RECYCLED
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.NEGATIVE,
            newState = CoronaTest.State.RECYCLED
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.POSITIVE,
            newState = CoronaTest.State.RECYCLED
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.POSITIVE,
            newState = CoronaTest.State.INVALID
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.NEGATIVE,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.POSITIVE,
            newState = CoronaTest.State.REDEEMED
        ) shouldBe false

        testHasResultInterestingChange(
            oldState = CoronaTest.State.NEGATIVE,
            newState = CoronaTest.State.INVALID
        ) shouldBe false
    }
}
