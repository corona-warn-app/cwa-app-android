package de.rki.coronawarnapp.util.reset

import io.github.classgraph.ClassGraph
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ResetInjectionTest : BaseTest() {

    /**
     * Scan our class graph, and compare it against what we inject via Dagger.
     * Check if an [Resettable] was accidentally forgotten.
     */
    @Test
    fun `all resettable are injected`() {
        val resettableSet = DaggerResetTestComponent.create().resettableSet

        println("We know ${resettableSet.size} resettable")
        resettableSet.isNotEmpty() shouldBe true

        val scanResult = ClassGraph()
            .acceptPackages("de.rki.coronawarnapp")
            .enableClassInfo()
            .scan()

        val resettableClasses = scanResult
            .getClassesImplementing(Resettable::class.java)
            .filterNot { it.isAbstract || it.isAnonymousInnerClass }

        println("Our project contains ${resettableClasses.size} resettable classes")
        val injected = resettableSet.map { it::class.java.simpleName }.toSet()
        val existing = resettableClasses.map { it.simpleName }.toSet()
        injected shouldContainAll existing
    }
}
