package exchange.dydx.trading

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import exchange.dydx.trading.TradingTestActivity.C
import exchange.dydx.trading.TradingTestActivity.C.Screen

class TradingTestActivity : ComponentActivity() {

    object C {
        object Tag {
            const val main_screen_container = "main_screen_container"
            const val main_screen_button = "main_screen_button"
        }

        object Screen {
            const val main_screen = "main_screen"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StartNavigation()
        }
    }

    @Composable
    private fun StartNavigation() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = C.Screen.main_screen) {
            composable(C.Screen.main_screen) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .semantics { testTag = C.Tag.main_screen_container },
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = C.Tag.main_screen_button },
                        content = { Text("Button") },
                        onClick = { navController.navigate(Screen.main_screen) },
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    simpleFlakyClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .semantics { testTag = C.Tag.main_screen_container },
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = C.Tag.main_screen_button },
            content = { Text("Button") },
            onClick = simpleFlakyClick,
        )
    }
}
