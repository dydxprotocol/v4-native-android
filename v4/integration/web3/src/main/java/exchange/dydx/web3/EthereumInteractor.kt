package exchange.dydx.web3

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.util.concurrent.Executors

class EthereumInteractor(
    private val rpcEndpoint: String,
) {
    private val web3j = Web3j.build(HttpService(rpcEndpoint))

    private val executor = Executors.newFixedThreadPool(4)

    fun netVersion(completion: (error: Exception?, networkVersion: String?) -> Unit) {
        executor.submit {
            try {
                val netVersion = web3j.netVersion().send()
                completion(null, netVersion.netVersion)
            } catch (e: Exception) {
                completion(e, null)
            }
        }
    }

    fun ethGasPrice(completion: (error: Exception?, gasPrice: BigInteger?) -> Unit) {
        executor.submit {
            try {
                val gasPrice = web3j.ethGasPrice().send()
                runOnMainThread {
                    completion(null, gasPrice.gasPrice)
                }
            } catch (e: Exception) {
                runOnMainThread {
                    completion(e, null)
                }
            }
        }
    }

    fun ethBlockNumber(completion: (error: Exception?, blockNumber: BigInteger?) -> Unit) {
        executor.submit {
            try {
                val blockNumber = web3j.ethBlockNumber().send()
                runOnMainThread {
                    completion(null, blockNumber.blockNumber)
                }
            } catch (e: Exception) {
                runOnMainThread {
                    completion(e, null)
                }
            }
        }
    }

    fun ethGetTransactionCount(
        address: String,
        completion: (error: Exception?, nonce: BigInteger?) -> Unit
    ) {
        executor.submit {
            try {
                val nonce = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send()
                runOnMainThread {
                    completion(null, nonce.transactionCount)
                }
            } catch (e: Exception) {
                runOnMainThread {
                    completion(e, null)
                }
            }
        }
    }

    fun ethGetBalance(
        address: String,
        completion: (error: Exception?, balance: BigInteger?) -> Unit
    ) {
        executor.submit {
            try {
                val balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
                runOnMainThread {
                    completion(null, balance)
                }
            } catch (e: Exception) {
                runOnMainThread {
                    completion(e, null)
                }
            }
        }
    }

    fun erc20TokenGetBalance(
        accountAddress: String,
        tokenAddress: String,
        completion: (error: Exception?, balance: BigInteger?) -> Unit
    ) {
        val function = Function(
            "balanceOf",
            listOf(Address(accountAddress)),
            listOf(object : TypeReference<Uint256?>() {}),
        )
        val encodedFunction = FunctionEncoder.encode(
            function,
        )

        val transaction = Transaction.createEthCallTransaction(
            accountAddress,
            tokenAddress,
            encodedFunction,
        )

        ethCall(transaction) { error, value ->
            if (error == null && value != null) {
                val someTypes = FunctionReturnDecoder.decode(
                    value,
                    function.outputParameters,
                )
                if (someTypes.isNotEmpty()) {
                    val balance = someTypes[0].value as BigInteger
                    completion(null, balance)
                } else {
                    completion(java.lang.Exception("Unexpected response"), null)
                }
            } else {
                completion(error, null)
            }
        }
    }

    fun erc20GetAllowance(
        tokenAddress: String,
        ownerAddress: String,
        spenderAddress: String,
        completion: (error: Exception?, balance: BigInteger?) -> Unit
    ) {
        val function = Function(
            "allowance",
            listOf(Address(ownerAddress), Address(spenderAddress)),
            listOf(object : TypeReference<Uint256?>() {}),
        )
        val encodedFunction = FunctionEncoder.encode(
            function,
        )

        val transaction = Transaction.createEthCallTransaction(
            ownerAddress,
            tokenAddress,
            encodedFunction,
        )

        ethCall(transaction) { error, value ->
            if (error == null && value != null) {
                val someTypes = FunctionReturnDecoder.decode(
                    value,
                    function.outputParameters,
                )
                if (someTypes.isNotEmpty()) {
                    val balance = someTypes[0].value as BigInteger
                    completion(null, balance)
                } else {
                    completion(java.lang.Exception("Unexpected response"), null)
                }
            } else {
                completion(error, null)
            }
        }
    }

    fun ethEstimateGas(completion: (error: Exception?, gas: BigInteger?) -> Unit) {
        executor.submit {
            try {
                val gas = web3j.ethEstimateGas(null).send()
                runOnMainThread {
                    completion(null, gas.amountUsed)
                }
            } catch (e: Exception) {
                runOnMainThread {
                    completion(e, null)
                }
            }
        }
    }

    fun ethSendRawTransaction(
        signedTransactionData: String,
        completion: (error: Exception?, txHash: String?) -> Unit
    ) {
        executor.submit {
            try {
                val txHash = web3j.ethSendRawTransaction(signedTransactionData).send()
                runOnMainThread {
                    completion(null, txHash.transactionHash)
                }
            } catch (e: Exception) {
                runOnMainThread {
                    completion(e, null)
                }
            }
        }
    }

    fun ethCall(
        transaction: org.web3j.protocol.core.methods.request.Transaction,
        completion: (error: Exception?, result: String?) -> Unit
    ) {
        executor.submit {
            try {
                val result = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send()
                runOnMainThread {
                    completion(null, result.value)
                }
            } catch (e: Exception) {
                runOnMainThread {
                    completion(e, null)
                }
            }
        }
    }

    private val mainScope = CoroutineScope(Dispatchers.Main)

    private fun runOnMainThread(completion: () -> Unit) {
        mainScope.launch { completion() }
    }
}
