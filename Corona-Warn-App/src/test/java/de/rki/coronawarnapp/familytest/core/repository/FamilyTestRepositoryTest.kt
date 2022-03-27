package de.rki.coronawarnapp.familytest.core.repository

import io.mockk.MockKAnnotations
import org.junit.jupiter.api.BeforeEach
import testhelpers.BaseTest

class FamilyTestRepositoryTest : BaseTest() {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }
}
