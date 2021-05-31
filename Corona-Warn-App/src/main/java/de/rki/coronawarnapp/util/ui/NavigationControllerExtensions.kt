package de.rki.coronawarnapp.util.ui

import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavGraph

/**
 * Finds nested graph [NavGraph] by Id.
 * @param nestedGraphId
 * @throws IllegalArgumentException if graph not found
 */
fun NavController.findNestedGraph(@IdRes nestedGraphId: Int): NavGraph {
    return graph.findNode(nestedGraphId) as? NavGraph
        ?: throw IllegalArgumentException("Nested graph with id=$nestedGraphId not found")
}
