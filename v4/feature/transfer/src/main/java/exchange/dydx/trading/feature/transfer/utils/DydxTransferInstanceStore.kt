package exchange.dydx.trading.feature.transfer.utils

import exchange.dydx.abacus.output.input.TransferInput
import exchange.dydx.abacus.output.input.TransferType
import exchange.dydx.abacus.protocols.ParserProtocol
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferInstance
import exchange.dydx.dydxstatemanager.clientState.transfers.DydxTransferStateManagerProtocol
import java.time.Instant
import javax.inject.Inject

interface DydxTransferInstanceStoring {
    fun addTransferHash(hash: String, fromChainName: String?, toChainName: String?, transferInput: TransferInput)
}

class DydxTransferInstanceStore @Inject constructor(
    private val transferStateManager: DydxTransferStateManagerProtocol,
    private val parser: ParserProtocol,
) : DydxTransferInstanceStoring {
    override fun addTransferHash(
        hash: String,
        fromChainName: String?,
        toChainName: String?,
        transferInput: TransferInput,
    ) {
        val transfer = DydxTransferInstance(
            transferType = when (transferInput.type) {
                TransferType.deposit -> DydxTransferInstance.TransferType.DEPOSIT
                TransferType.withdrawal -> DydxTransferInstance.TransferType.WITHDRAWAL
                TransferType.transferOut -> DydxTransferInstance.TransferType.TRANSFER_OUT
                else -> DydxTransferInstance.TransferType.DEPOSIT
            },
            transactionHash = hash.lowercase(),
            fromChainId = transferInput.requestPayload?.fromChainId,
            fromChainName = fromChainName,
            toChainId = transferInput.requestPayload?.toChainId,
            toChainName = toChainName,
            dateIntoEpochMilli = Instant.now().toEpochMilli(),
            usdcSize = parser.asDouble(transferInput.size?.usdcSize),
            size = parser.asDouble(transferInput.size?.size),
            isCctp = transferInput.isCctp,
            requestId = transferInput.requestPayload?.requestId,
        )
        transferStateManager.add(transfer)
    }
}

val TransferInput.chainName: String?
    get() {
        return if (chain != null) {
            resources?.chainResources?.get(chain)?.chainName
        } else {
            null
        }
    }

val TransferInput.networkName: String?
    get() {
        return if (chain != null) {
            resources?.chainResources?.get(chain)?.networkName
        } else {
            null
        }
    }
