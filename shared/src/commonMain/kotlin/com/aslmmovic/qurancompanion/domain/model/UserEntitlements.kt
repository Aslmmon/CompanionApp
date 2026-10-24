package com.aslmmovic.qurancompanion.domain.model

data class UserEntitlements(
    val isPatron: Boolean = false,
    val isSupporter: Boolean = false,
    val expirationTimestamp: Long? = null
)
