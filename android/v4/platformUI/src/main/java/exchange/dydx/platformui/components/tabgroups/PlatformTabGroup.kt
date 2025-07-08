package exchange.dydx.platformui.components.tabgroups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.unit.dp
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun PlatformTabGroup(
    modifier: Modifier = Modifier,
    items: List<@Composable (modifier: Modifier) -> Unit>,
    selectedItems: List<@Composable (modifier: Modifier) -> Unit>,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(ThemeShapes.HorizontalPadding),
    equalWeight: Boolean = false,
    scrollingEnabled: Boolean = true,
    currentSelection: Int? = null,
    onSelectionChanged: (Int) -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val selectionChangeInProgress = remember { MutableStateFlow(false) }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
    ) {
        items.forEachIndexed { index, item ->
            if (index == currentSelection) {
                Box(
                    modifier = Modifier
                        .padding(start = if (scrollingEnabled && index == 0) ThemeShapes.HorizontalPadding else 0.dp)
                        .padding(end = if (scrollingEnabled && index == items.count() - 1) ThemeShapes.HorizontalPadding else 0.dp)
                        .then(
                            if (equalWeight) {
                                Modifier.weight(1f)
                            } else {
                                Modifier
                            },
                        )
                        .onGloballyPositioned { coordinates ->
                            if (scrollingEnabled && selectionChangeInProgress.value) {
                                selectionChangeInProgress.value = false
                                val size = coordinates.size
                                val position = coordinates.positionInParent()
                                val scrollPosition = scrollState.value
                                val viewportSize = scrollState.viewportSize
                                if (scrollPosition > position.x) {
                                    scope.launch {
                                        scrollState.animateScrollTo(position.x.toInt())
                                    }
                                } else if (scrollPosition + viewportSize < position.x + size.width) {
                                    scope.launch {
                                        scrollState.animateScrollTo(
                                            position.x.toInt() + size.width - viewportSize,
                                        )
                                    }
                                }
                            }
                        },
                ) {
                    selectedItems[index](
                        Modifier,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = if (scrollingEnabled && index == 0) ThemeShapes.HorizontalPadding else 0.dp)
                        .padding(end = if (scrollingEnabled && index == items.count() - 1) ThemeShapes.HorizontalPadding else 0.dp)
                        .then(
                            if (equalWeight) {
                                Modifier.weight(1f)
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    item(
                        Modifier
                            .clickable {
                                if (index != currentSelection) {
                                    onSelectionChanged(index)
                                    if (scrollingEnabled) {
                                        selectionChangeInProgress.value = true
                                    }
                                }
                            },
                    )
                }
            }
        }
    }
}
