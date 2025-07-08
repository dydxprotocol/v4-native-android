package exchange.dydx.web3

import junit.framework.TestCase.assertEquals
import net.jodah.concurrentunit.Waiter
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun eth_interactor() {
        val waiter = Waiter()
        val interactor = EthereumInteractor("https://ethereum-sepolia-rpc.publicnode.com")
        interactor.netVersion { error, networkVersion ->
            println("error: $error")
            println("networkVersion: $networkVersion")
            waiter.resume()
        }
        waiter.await(5000)
    }
}
