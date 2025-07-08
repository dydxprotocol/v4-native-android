package exchange.dydx.web3

import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

object ABIEncoder {
    fun encodeERC20ApproveFunction(
        spenderAddress: String,
        desiredAmount: BigInteger = BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16),
    ): String {
        val function = Function(
            "approve",
            listOf(Address(spenderAddress), Uint256(desiredAmount)),
            listOf(),
        )
        return FunctionEncoder.encode(
            function,
        )
    }
}
