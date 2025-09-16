package com.aryanspatel.grofunds.domain.model

data class DraftRef(
    val id: String,
    val path: String,
    val kind: EntryKind
)