package de.rki.coronawarnapp.release

interface NewReleaseInfoItem {
    val title: String
    val body: String
}

data class NewReleaseInfoItemText(
    override val title: String,
    override val body: String
) : NewReleaseInfoItem

data class NewReleaseInfoItemLinked(
    override val title: String,
    override val body: String,
    val linkifiedLabel: String,
    val linkTarget: String
) : NewReleaseInfoItem
