package com.aryanspatel.grofunds.data.mapper

import com.aryanspatel.grofunds.data.local.entity.TransactionEntity
import com.aryanspatel.grofunds.data.model.RemoteTxn
import com.aryanspatel.grofunds.data.remote.model.TransactionDoc
import java.util.Date

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

// Mapper from your Room entity:
fun TransactionEntity.toRemoteDoc(): TransactionDoc = TransactionDoc(
    transactionID = transactionID,
    kind = kind,
    input = input,
    amount = amount,
    currency = currencyCode,
    categoryOrTypeID = categoryOrTypeID,
    subcategoryID = subcategoryID,
    merchant = merchant,
    note = note,
    date = Date(date),               // your millis -> Date
    status = "saved",
    updatedAt = null                 // important: let server set it
)