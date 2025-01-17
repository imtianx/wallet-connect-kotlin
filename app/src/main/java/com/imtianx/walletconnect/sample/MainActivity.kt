package com.imtianx.walletconnect.sample

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.imtianx.walletconnect.WCClient
import com.imtianx.walletconnect.exceptions.InvalidSessionException
import com.imtianx.walletconnect.models.WCAccount
import com.imtianx.walletconnect.models.WCPeerMeta
import com.imtianx.walletconnect.models.WCSignTransaction
import com.imtianx.walletconnect.models.binance.WCBinanceCancelOrder
import com.imtianx.walletconnect.models.binance.WCBinanceTradeOrder
import com.imtianx.walletconnect.models.binance.WCBinanceTransferOrder
import com.imtianx.walletconnect.models.binance.WCBinanceTxConfirmParam
import com.imtianx.walletconnect.models.ethereum.WCEthereumSignMessage
import com.imtianx.walletconnect.models.ethereum.WCEthereumTransaction
import com.imtianx.walletconnect.models.session.WCSession
import com.imtianx.walletconnect.sample.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import wallet.core.jni.CoinType
import wallet.core.jni.PrivateKey

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val wcClient by lazy {
        WCClient(GsonBuilder(), OkHttpClient())
    }

    val privateKey =
        PrivateKey("ba005cd605d8a02e3d5dfd04234cef3a3ee4f76bfbad2722d1fb5af8e12e6764".decodeHex())
    val address = CoinType.ETHEREUM.deriveAddress(privateKey)

    private val peerMeta = WCPeerMeta(name = "Example", url = "https://example.com")

    private lateinit var wcSession: WCSession

    private var remotePeerMeta: WCPeerMeta? = null

    companion object {
        init {
            System.loadLibrary("TrustWalletCore")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchBscButton.setOnClickListener {
            onWalletChangeNetwork(chainId = 56)
        }

        binding.uriInput.editText?.setText(
            "wc:5265cdfb-e4ff-4b3c-9b92-92267e90925d@1?bridge=https%3A%2F%2F8.bridge.walletconnect.org&key=abf021126d8a006e532298bf52c1e2d642c1d81f73f5b39652ade0d35114c3de"
        )

        binding.addressInput.editText?.setText(address)
        wcClient.onDisconnect = { _, _ -> onDisconnect() }
        wcClient.onFailure = { t -> onFailure(t) }
        wcClient.onSessionRequest = { _, peer -> onSessionRequest(peer) }
        wcClient.onGetAccounts = { id -> onGetAccounts(id) }

        wcClient.onEthSign = { id, message -> onEthSign(id, message) }
        wcClient.onEthSignTransaction = { id, transaction -> onEthTransaction(id, transaction) }
        wcClient.onEthSendTransaction =
            { id, transaction -> onEthTransaction(id, transaction, send = true) }

        wcClient.onBnbTrade = { id, order -> onBnbTrade(id, order) }
        wcClient.onBnbCancel = { id, order -> onBnbCancel(id, order) }
        wcClient.onBnbTransfer = { id, order -> onBnbTransfer(id, order) }
        wcClient.onBnbTxConfirm = { _, param -> onBnbTxConfirm(param) }
        wcClient.onSignTransaction = { id, transaction -> onSignTransaction(id, transaction) }
        wcClient.onWalletChangeNetwork = { id, chainId -> onWalletChangeNetwork(id, chainId) }

        setupConnectButton()
    }

    private fun setupConnectButton() {
        runOnUiThread {
            binding.connectButton.text = "Connect"
            binding.connectButton.setOnClickListener {
                connect(binding.uriInput.editText?.text?.toString() ?: return@setOnClickListener)
            }
        }
    }

    fun connect(uri: String) {
        disconnect()
        wcSession = WCSession.from(uri) ?: throw InvalidSessionException()
        wcClient.connect(wcSession, peerMeta)
    }

    fun disconnect() {
        if (wcClient.session != null) {
            wcClient.killSession()
        } else {
            wcClient.disconnect()
        }
    }

    fun approveSession() {
        val address = binding.addressInput.editText?.text?.toString() ?: address
        val chainId = binding.chainInput.editText?.text?.toString()?.toIntOrNull() ?: 1
        wcClient.approveSession(listOf(address), chainId)
        binding.connectButton.text = "Kill Session"
        binding.connectButton.setOnClickListener {
            disconnect()
        }
    }

    fun rejectSession() {
        wcClient.rejectSession()
        wcClient.disconnect()
    }

    fun rejectRequest(id: Long) {
        wcClient.rejectRequest(id, "User canceled")
    }

    private fun onDisconnect() {
        setupConnectButton()
    }

    private fun onFailure(throwable: Throwable) {
        throwable.printStackTrace()
    }

    private fun onSessionRequest(peer: WCPeerMeta) {
        runOnUiThread {
            remotePeerMeta = peer
            wcClient.remotePeerId ?: run {
                println("remotePeerId can't be null")
                return@runOnUiThread
            }
            val meta = remotePeerMeta ?: return@runOnUiThread
            AlertDialog.Builder(this)
                .setTitle(meta.name)
                .setMessage("${meta.description}\n${meta.url}")
                .setPositiveButton("Approve") { _, _ ->
                    approveSession()
                }
                .setNegativeButton("Reject") { _, _ ->
                    rejectSession()
                }
                .show()
        }
    }

    private fun onEthSign(id: Long, message: WCEthereumSignMessage) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle(message.type.name)
                .setMessage(message.data)
                .setPositiveButton("Sign") { _, _ ->
                    wcClient.approveRequest(
                        id,
                        privateKey.sign(message.data.decodeHex(), CoinType.ETHEREUM.curve())
                    )
                }
                .setNegativeButton("Cancel") { _, _ ->
                    rejectRequest(id)
                }
                .show()
        }
    }

    private fun onEthTransaction(id: Long, payload: WCEthereumTransaction, send: Boolean = false) {}

    private fun onBnbTrade(id: Long, order: WCBinanceTradeOrder) {}

    private fun onBnbCancel(id: Long, order: WCBinanceCancelOrder) {}

    private fun onBnbTransfer(id: Long, order: WCBinanceTransferOrder) {}

    private fun onBnbTxConfirm(param: WCBinanceTxConfirmParam) {}

    private fun onGetAccounts(id: Long) {
        val account = WCAccount(
            binding.chainInput.editText?.text?.toString()?.toIntOrNull() ?: 1,
            binding.addressInput.editText?.text?.toString() ?: address,
        )
        wcClient.approveRequest(id, account)
    }

    private fun onSignTransaction(id: Long, payload: WCSignTransaction) {}

    private fun onWalletChangeNetwork(id: Long? = null, chainId: Int) {
        if (!isFinishing) {
            runOnUiThread {
                binding.chainInput.editText?.setText("$chainId")
            }
            wcClient.switchChain(id, chainId)
        }
    }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return removePrefix("0x")
            .chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

}
