package com.aryanspatel.grofunds.domain.repository

import kotlinx.coroutines.flow.Flow

interface CurrentUserProvider {
    fun userIdOrNull() :  String?
}

interface CurrentUserFlow {
    val uidFlow: Flow<String?>
}