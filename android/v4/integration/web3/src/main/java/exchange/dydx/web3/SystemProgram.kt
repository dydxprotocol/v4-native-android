package exchange.dydx.carteraexample.solana

import com.solana.publickey.SolanaPublicKey
import com.solana.transaction.AccountMeta
import com.solana.transaction.TransactionInstruction
import java.nio.ByteBuffer
import java.nio.ByteOrder

object SystemProgram {
    val programId = SolanaPublicKey.from("11111111111111111111111111111111")

    fun transfer(
        fromPublicKey: SolanaPublicKey,
        toPublicKey: SolanaPublicKey,
        lamports: Long
    ): TransactionInstruction {
        val accounts = listOf(
            AccountMeta(fromPublicKey, isSigner = true, isWritable = true),
            AccountMeta(toPublicKey, isSigner = false, isWritable = true),
        )

        // SystemProgram Instruction Layout:
        // 4 bytes for instruction (u32 LE) + 8 bytes for amount (u64 LE)
        val instructionIndex = 2 // Transfer instruction index in SystemProgram
        val buffer = ByteBuffer.allocate(12)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(instructionIndex) // instruction enum
        buffer.putLong(lamports)

        return TransactionInstruction(
            programId = programId,
            accounts = accounts,
            data = buffer.array(),
        )
    }
}
