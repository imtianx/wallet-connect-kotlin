package com.imtianx.walletconnect.models

import com.google.gson.annotations.SerializedName

enum class WCMethod {
    @SerializedName("wc_sessionRequest")
    SESSION_REQUEST,

    @SerializedName("wc_sessionUpdate")
    SESSION_UPDATE,

    @SerializedName("eth_sign")
    ETH_SIGN,

    @SerializedName("personal_sign")
    ETH_PERSONAL_SIGN,

    @SerializedName("eth_signTypedData")
    ETH_SIGN_TYPE_DATA,

    @SerializedName("eth_signTypedData_v4")
    ETH_SIGN_TYPE_DATA_V4,

    @SerializedName("eth_signTransaction")
    ETH_SIGN_TRANSACTION,

    @SerializedName("eth_sendTransaction")
    ETH_SEND_TRANSACTION,

    @SerializedName("bnb_sign")
    BNB_SIGN,

    @SerializedName("bnb_tx_confirmation")
    BNB_TRANSACTION_CONFIRM,

    @SerializedName("get_accounts")
    GET_ACCOUNTS,

    @SerializedName("trust_signTransaction")
    SIGN_TRANSACTION,

    @SerializedName("wallet_switchEthereumChain")
    WALLET_SWITCH_NETWORK,

    @SerializedName("wallet_addEthereumChain")
    WALLET_ADD_NETWORK,
    
    @SerializedName("aptos_sign")
    APTOS_SIGN,

    @SerializedName("aptos_signTransaction")
    APTOS_SIGNTRANSACTION,

    @SerializedName("aptos_sendTransaction")
    APTOS_SENDTRANSACTION,
    
    ;
    
}