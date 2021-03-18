package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

//Determine Normalized Time per Check-in: the normalized time per check-in is
// the sum of all Normalized Time per Match of the corresponding matches.
//
//Determine Risk Level: find the first item in Configuration Parameter normalizedTimePerCheckInToRiskLevelMapping
// where Normalized Time per Check-in of the Check-In is in the range of the item described by
// normalizedTimeRange (see Working with Ranges).
// The Risk Level is the riskLevel parameter of the matching item.
// If there is no matching item, no Risk Level is associated with the Check-in
// (i.e. the Check-in is not relevant from an epidemiological perspective).
