package de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets

data class DefaultValueSet(
    override val items: List<ValueSets.ValueSet.Item> = emptyList()
) : ValueSets.ValueSet {
    data class Item(
        override val key: String,
        override val displayText: String
    ) : ValueSets.ValueSet.Item
}
