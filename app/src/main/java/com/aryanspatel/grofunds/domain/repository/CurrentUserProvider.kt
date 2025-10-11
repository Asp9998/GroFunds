package com.aryanspatel.grofunds.domain.repository

interface CurrentUserProvider {
    fun userIdOrNull() :  String?
}