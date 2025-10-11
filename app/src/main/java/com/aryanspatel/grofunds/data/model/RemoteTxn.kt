package com.aryanspatel.grofunds.data.model

data class RemoteTxn(
    val transactionID: String,
    val kind: String?,
    val input: String?,
    val amount: Double?,
    val currency: String?,
    val categoryID: String?,
    val subcategoryID: String?,
    val merchant: String?,
    val note: String?,
    val dateMillis: Long?,            // from Firestore Timestamp -> millis
    val status: String?,
    val updatedAtMillis: Long?,       // from Firestore Timestamp -> millis
)