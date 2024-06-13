package exchange.dydx.trading.feature.shared.views

import android.widget.Switch
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import exchange.dydx.abacus.protocols.LocalizerProtocol
import exchange.dydx.platformui.components.dividers.PlatformDivider
import exchange.dydx.platformui.components.icons.PlatformSelectedIcon
import exchange.dydx.platformui.components.icons.PlatformUnselectedIcon
import exchange.dydx.platformui.components.inputs.PlatformTextInput
import exchange.dydx.platformui.components.tabgroups.PlatformTextTabGroup
import exchange.dydx.platformui.designSystem.theme.ThemeColor
import exchange.dydx.platformui.designSystem.theme.ThemeFont
import exchange.dydx.platformui.designSystem.theme.ThemeShapes
import exchange.dydx.platformui.designSystem.theme.color
import exchange.dydx.platformui.designSystem.theme.dydxDefault
import exchange.dydx.platformui.designSystem.theme.themeColor
import exchange.dydx.platformui.designSystem.theme.themeFont
import exchange.dydx.platformui.settings.PlatformUISettings
import exchange.dydx.platformui.theme.DydxThemedPreviewSurface
import exchange.dydx.platformui.theme.MockLocalizer
import exchange.dydx.trading.feature.shared.R

@Preview
@Composable
fun Preview_SettingsView() {
    DydxThemedPreviewSurface {
        SettingsView.Content(Modifier, SettingsView.ViewState.preview)
    }
}

object SettingsView {

    data class ViewState(
        val localizer: LocalizerProtocol,
        val header: String? = null,
        val footer: String? = null,
        val sections: List<Section> = emptyList(),
        val backAction: (() -> Unit)? = null,
        val itemAction: ((String) -> Unit)? = null,
    ) {

        data class Section(
            val title: String? = null,
            val items: List<Item> = emptyList(),
        ) {
            companion object {
                val preview = Section(
                    title = "title",
                    items = listOf(
                        Item.preview,
                        Item.preview,
                    ),
                )
            }
        }

        data class Item(
            val title: String? = null,
            val subtitle: String? = null,
            var value: String? = null,
            val route: String? = null,
            var selected: Boolean? = null,
            val field: ItemField? = null,
        ) {
            companion object {
                val preview = Item(
                    title = "title",
                    subtitle = "subtitle",
                    value = "value",
                    route = "route",
                    selected = true,
                    field = ItemField.preview,
                )
            }
        }

        data class ItemFieldOption(
            val text: String? = null,
            val value: String? = null,
            var selected: Boolean? = null,
        ) {
            companion object {
                val preview = ItemFieldOption(
                    text = "text",
                    value = "value",
                )
            }
        }

        enum class ItemFieldType {
            SELECT,
            SWITCH;

            companion object {
                fun fromString(value: String?): ItemFieldType? {
                    return when (value) {
                        "select" -> SELECT
                        "bool" -> SWITCH
                        else -> null
                    }
                }
            }
        }

        data class ItemField(
            val fieldId: String? = null,
            val type: ItemFieldType? = null,
            val options: List<ItemFieldOption>? = null,
            val fieldAction: ((String) -> Unit)? = null,
        ) {
            companion object {
                val preview = ItemField(
                    fieldId = "fieldId",
                    options = listOf(
                        ItemFieldOption.preview,
                        ItemFieldOption.preview,
                    ),
                )
            }
        }

        companion object {
            val preview = ViewState(
                localizer = MockLocalizer(),
                header = "header",
                footer = "footer",
                sections = listOf(
                    Section.preview,
                    Section.preview,
                ),
            )

            fun createFrom(
                settings: PlatformUISettings,
                localizer: LocalizerProtocol,
                header: String?,
                footer: String? = null,
                backAction: (() -> Unit)? = null,
                itemAction: ((String) -> Unit)? = null,
                itemFieldAction: ((String, String) -> Unit)? = null,
                valueOfField: ((PlatformUISettings.Field) -> String?)? = null,
            ): ViewState {
                return ViewState(
                    localizer = localizer,
                    header = header,
                    footer = footer,
                    sections = settings.sections.map { section ->
                        Section(
                            title = section.title,
                            items = if (section.fields == null) {
                                emptyList()
                            } else if (section.fields!!.size > 1) {
                                section.fields!!.map { item ->
                                    createSingleLineFieldItem(
                                        item = item,
                                        itemFieldAction = itemFieldAction,
                                        valueOfField = valueOfField,
                                    )
                                }
                            } else {
                                val sectionField = section.fields!!.first()
                                if (sectionField.field?.type == "bool") {
                                    val item = createSingleLineFieldItem(
                                        item = sectionField,
                                        itemFieldAction = itemFieldAction,
                                        valueOfField = valueOfField,
                                    )
                                    listOf(item)
                                } else {
                                    sectionField.field?.options?.map { option ->
                                        Item(
                                            title = option.text,
                                            value = option.value,
                                            route = null,
                                        )
                                    } ?: emptyList()
                                }
                            },
                        )
                    },
                    backAction = backAction,
                    itemAction = itemAction,
                )
            }

            private fun createSingleLineFieldItem(
                item: PlatformUISettings.SectionField,
                itemFieldAction: ((String, String) -> Unit)? = null,
                valueOfField: ((PlatformUISettings.Field) -> String?)? = null
            ): Item {
                return Item(
                    title = item.title?.text,
                    value = item.field?.let {
                        valueOfField?.invoke(it)
                    } ?: run {
                        item.text?.text
                    },
                    route = item.link?.text,
                    field = item.field?.let {
                        ItemField(
                            fieldId = it.field,
                            type = ItemFieldType.fromString(it.type),
                            options = it.options?.map { option ->
                                ItemFieldOption(
                                    text = option.text,
                                    value = option.value,
                                    selected = option.value == valueOfField?.invoke(it),
                                )
                            },
                            fieldAction = { value ->
                                itemFieldAction?.invoke(it.field ?: "", value)
                            },
                        )
                    },
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content(modifier: Modifier, state: ViewState?) {
        if (state == null) return

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        Box(
            modifier = modifier
                .fillMaxSize()
                .themeColor(ThemeColor.SemanticColor.layer_2),
        ) {
            Column {
                HeaderView(
                    title = state.header ?: "",
                    backAction = state.backAction,
                )
                PlatformDivider()
                LazyColumn(
                    modifier = Modifier,
                    state = listState,
                ) {
                    state.sections.forEach { section ->
                        if (section.title != null) {
                            stickyHeader {
                                Column(
                                    Modifier
                                        .fillParentMaxWidth()
                                        .padding(vertical = ThemeShapes.VerticalPadding)
                                        .padding(start = 16.dp)
                                        .themeColor(ThemeColor.SemanticColor.layer_2),
                                ) {
                                    Text(
                                        text = section.title,
                                        style = TextStyle.dydxDefault
                                            .themeFont(fontSize = ThemeFont.FontSize.large)
                                            .themeColor(ThemeColor.SemanticColor.text_primary),
                                    )
                                }
                            }
                        }

                        items(items = section.items) { item ->
                            CreateSectionItem(item = item, state = state)
                            if (item != section.items.last()) {
                                PlatformDivider()
                            }
                        }
                    }

                    item(key = "footer") {
                        PlatformDivider()
                        CreateFooter(state = state)
                    }
                }
            }
        }
    }

    @Composable
    private fun CreateSectionItem(
        modifier: Modifier = Modifier,
        item: ViewState.Item,
        state: ViewState
    ) {
        if (item.field != null && item.route == null) {
            CreateSectionFieldItem(
                modifier = modifier,
                item = item,
                state = state,
            )
        } else {
            CreateSectionLineItem(
                modifier = modifier,
                item = item,
                state = state,
            )
        }
    }

    @Composable
    private fun CreateSectionFieldItem(
        modifier: Modifier = Modifier,
        item: ViewState.Item,
        state: ViewState
    ) {
        @Composable
        fun CreateFieldTitle() {
            Text(
                text = if (item.title != null) state.localizer.localize(item.title!!) else "",
                style = TextStyle.dydxDefault
                    .themeFont(fontSize = ThemeFont.FontSize.base)
                    .themeColor(ThemeColor.SemanticColor.text_primary),
            )
        }

        val field = item.field ?: return

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(ThemeShapes.VerticalPadding),
        ) {
            if (field.options == null) {
                Column(
                    modifier = modifier.padding(vertical = 16.dp),
                ) {
                    CreateFieldTitle()
                    PlatformTextInput(
                        modifier = Modifier
                            .height(40.dp)
                            .background(color = ThemeColor.SemanticColor.layer_4.color),
                        value = item.value ?: "",
                        onValueChange = { value ->
                            item.field.fieldAction?.invoke(value)
                        },
                    )
                }
            } else {
                when (field.type) {
                    ViewState.ItemFieldType.SELECT, null -> {
                        Column(
                            modifier = modifier.padding(vertical = 16.dp),
                        ) {
                            CreateFieldTitle()
                            Row {
                                Spacer(modifier = Modifier.weight(1f))
                                PlatformTextTabGroup(
                                    items = field.options.map { it.text ?: "" },
                                    selectedItems = field.options.map { it.text ?: "" },
                                    currentSelection = field.options.indexOfFirst { it.selected == true },
                                    onSelectionChanged = { index ->
                                        field.options.forEachIndexed { i, option ->
                                            option.selected = i == index
                                        }
                                        item.field.fieldAction?.invoke(
                                            field.options[index].value ?: "",
                                        )
                                    },
                                )
                            }
                        }
                    }

                    ViewState.ItemFieldType.SWITCH -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            CreateFieldTitle()
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = item.value == "1",
                                onCheckedChange = {
                                    item.field.fieldAction?.invoke(if (it) "1" else "0")
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CreateSectionLineItem(
        modifier: Modifier = Modifier,
        item: ViewState.Item,
        state: ViewState
    ) {
        Row(
            modifier
                .fillMaxWidth()
                .clickable {
                    item.route?.let { route -> state.itemAction?.invoke(route) }
                    if (item.selected != null && item.value != null) {
                        state.itemAction?.invoke(item.value!!)
                    }
                }
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = 16.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
            ) {
                Text(
                    text = if (item.title != null) state.localizer.localize(item.title) else "",
                    style = TextStyle.dydxDefault
                        .themeFont(fontSize = ThemeFont.FontSize.base)
                        .themeColor(ThemeColor.SemanticColor.text_primary),
                )
            }

            if (item.selected == null) {
                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Text(
                        text = item.value ?: "",
                        style = TextStyle.dydxDefault
                            .themeFont(fontSize = ThemeFont.FontSize.base)
                            .themeColor(ThemeColor.SemanticColor.text_secondary),
                    )
                }

                if (item.route != null) {
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Icon(
                            painter = painterResource(id = R.drawable.chevron_right),
                            contentDescription = "",
                            modifier = Modifier
                                .size(16.dp)
                                .padding(start = 8.dp),
                            tint = ThemeColor.SemanticColor.text_primary.color,
                        )
                    }
                }
            } else {
                if (item.selected == false) {
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        PlatformUnselectedIcon()
                    }
                } else if (item.selected == true) {
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        PlatformSelectedIcon()
                    }
                }
            }
        }
    }

    @Composable
    private fun CreateFooter(modifier: Modifier = Modifier, state: ViewState) {
        Row(
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ThemeShapes.HorizontalPadding,
                    vertical = ThemeShapes.VerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = state.footer ?: "",
                style = TextStyle.dydxDefault
                    .themeFont(
                        fontSize = ThemeFont.FontSize.small,
                    )
                    .themeColor(ThemeColor.SemanticColor.text_tertiary),
            )
        }
    }
}
