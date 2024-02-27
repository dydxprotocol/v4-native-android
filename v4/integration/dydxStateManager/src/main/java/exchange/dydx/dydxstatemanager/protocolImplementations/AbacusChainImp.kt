package exchange.dydx.dydxstatemanager.protocolImplementations

import exchange.dydx.abacus.protocols.DYDXChainTransactionsProtocol
import exchange.dydx.abacus.protocols.QueryType
import exchange.dydx.abacus.protocols.TransactionType
import exchange.dydx.trading.integration.cosmos.CosmosV4ClientProtocol

class AbacusChainImp(
    private val cosmosClient: CosmosV4ClientProtocol,
) : DYDXChainTransactionsProtocol {

    override fun connectNetwork(
        paramsInJson: String,
        callback: (response: String?) -> Unit,
    ) {
        cosmosClient.connectNetwork(paramsInJson, callback)
    }

    override fun get(
        type: QueryType,
        paramsInJson: String?,
        callback: (response: String?) -> Unit,
    ) {
        cosmosClient.call(type.rawValue, paramsInJson, callback)
    }

    override fun transaction(
        type: TransactionType,
        paramsInJson: String?,
        callback: (response: String?) -> Unit,
    ) {
        cosmosClient.call(type.rawValue, paramsInJson, callback)
    }
}
