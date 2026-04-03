package com.aryanspatel.grofunds.data.mapper

import com.aryanspatel.grofunds.data.local.entity.TransactionEntity
import com.aryanspatel.grofunds.data.model.RemoteTxn

fun RemoteTxn.toEntity(userId: String, previousLocalUpdatedAt: Long?): TransactionEntity {
    val remoteMillis = updatedAtMillis ?: 0L
    return TransactionEntity(
        transactionID = transactionID,
        userId = userId,
        kind = (kind ?: "EXPENSE"),
        amount = (amount ?: 0.0),
        currencyCode = (currency ?: "CAD"),
        categoryOrTypeID = (categoryID ?: ""),
        subcategoryID = subcategoryID,
        merchant = merchant,
        note = note,
        date = (dateMillis ?: 0L),
        remoteUpdatedAt = remoteMillis,
        localUpdatedAt = previousLocalUpdatedAt ?: remoteMillis,
        isDirty = false,
        input = (note ?: ""),
        createdAtUTC = remoteMillis,
    )
}