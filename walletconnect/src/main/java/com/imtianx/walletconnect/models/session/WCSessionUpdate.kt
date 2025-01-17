package com.imtianx.walletconnect.models.session

data class WCSessionUpdate(
    val approved: Boolean,
    val chainId: Int?,
    val accounts: List<String>?,
    val aptosAccounts: List<String>?,
    val chain: String?,//chain name
)