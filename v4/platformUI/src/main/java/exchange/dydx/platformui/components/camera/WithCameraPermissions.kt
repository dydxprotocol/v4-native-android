package exchange.dydx.platformui.components.camera

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import exchange.dydx.platformui.components.buttons.PlatformButton
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import kotlinx.serialization.json.JsonNull.content

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WithCameraPermissions(
    modifier: Modifier = Modifier,
    promptText: String = "Camera permission required for this feature to be available. Please grant the permission",
    buttonText: String = "Request permission",
    content: @Composable (Modifier) -> Unit,
) {
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA,
    )

    when (val status = cameraPermissionState.status) {
        // If the camera permission is granted, then show screen with the feature enabled
        PermissionStatus.Granted -> {
            content(modifier)
        }
        is PermissionStatus.Denied -> {
            Column(
                modifier = modifier,
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = promptText,
                    style = TextStyle.dydxDefault,
                )

                Spacer(modifier = Modifier.weight(1f))

                PlatformButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = buttonText,
                ) {
                    cameraPermissionState.launchPermissionRequest()
                }
            }
        }
    }
}
