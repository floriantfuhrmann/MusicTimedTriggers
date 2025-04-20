package eu.florian_fuhrmann.musictimedtriggers.gui.styles

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.styling.MenuItemMetrics
import org.jetbrains.jewel.ui.component.styling.MenuMetrics
import org.jetbrains.jewel.ui.component.styling.MenuStyle
import org.jetbrains.jewel.ui.theme.menuStyle
import org.jetbrains.jewel.ui.theme.simpleListItemStyle

object MenuStyles {
    val spaciousMenuStyle: MenuStyle
        @Composable
        get() = MenuStyle(
            isDark = JewelTheme.menuStyle.isDark,
            colors = JewelTheme.menuStyle.colors,
            metrics = MenuMetrics(
                cornerSize = JewelTheme.menuStyle.metrics.cornerSize,
                menuMargin = JewelTheme.menuStyle.metrics.menuMargin,
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                offset = JewelTheme.menuStyle.metrics.offset,
                shadowSize = JewelTheme.menuStyle.metrics.shadowSize,
                borderWidth = JewelTheme.menuStyle.metrics.borderWidth,
                itemMetrics = MenuItemMetrics(
                    selectionCornerSize = JewelTheme.simpleListItemStyle.metrics.selectionBackgroundCornerSize,
                    outerPadding = JewelTheme.menuStyle.metrics.itemMetrics.outerPadding,
                    contentPadding = JewelTheme.menuStyle.metrics.itemMetrics.contentPadding,
                    separatorPadding = JewelTheme.menuStyle.metrics.itemMetrics.separatorPadding,
                    keybindingsPadding = JewelTheme.menuStyle.metrics.itemMetrics.keybindingsPadding,
                    separatorThickness = JewelTheme.menuStyle.metrics.itemMetrics.separatorThickness,
                    separatorHeight = JewelTheme.menuStyle.metrics.itemMetrics.separatorHeight,
                    iconSize = JewelTheme.menuStyle.metrics.itemMetrics.iconSize,
                    minHeight = JewelTheme.menuStyle.metrics.itemMetrics.minHeight
                ),
                submenuMetrics = JewelTheme.menuStyle.metrics.submenuMetrics
            ),
            icons = JewelTheme.menuStyle.icons
        )
}