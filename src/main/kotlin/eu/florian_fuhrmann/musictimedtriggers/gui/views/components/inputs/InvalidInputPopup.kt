package eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.PopupContainer
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.styling.PopupContainerColors
import org.jetbrains.jewel.ui.component.styling.PopupContainerStyle
import org.jetbrains.jewel.ui.theme.defaultBannerStyle
import org.jetbrains.jewel.ui.theme.popupContainerStyle

private val invalidInputPopupContainerStyle: PopupContainerStyle
    @Composable
    get() = PopupContainerStyle(
        isDark = JewelTheme.popupContainerStyle.isDark,
        colors = PopupContainerColors(
            background = JewelTheme.defaultBannerStyle.error.colors.background,
            border = JewelTheme.defaultBannerStyle.error.colors.border,
            shadow = JewelTheme.popupContainerStyle.colors.shadow
        ),
        metrics = JewelTheme.popupContainerStyle.metrics
    )

@Composable
fun InvalidInputPopup(message: String) {
    PopupContainer(
        onDismissRequest = {},
        horizontalAlignment = Alignment.Start,
        popupProperties = PopupProperties(focusable = false),
        style = invalidInputPopupContainerStyle
    ) {
        Box(Modifier.padding(8.dp)) {
            Text(message)
        }
    }
}