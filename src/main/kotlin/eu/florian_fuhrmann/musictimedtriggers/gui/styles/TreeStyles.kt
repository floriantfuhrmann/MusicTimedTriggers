package eu.florian_fuhrmann.musictimedtriggers.gui.styles

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.styling.LazyTreeColors
import org.jetbrains.jewel.ui.component.styling.LazyTreeMetrics
import org.jetbrains.jewel.ui.component.styling.LazyTreeStyle
import org.jetbrains.jewel.ui.theme.treeStyle

val listTreeStyle: LazyTreeStyle
    @Composable
    get() {
        return LazyTreeStyle(
            LazyTreeColors(
                elementBackgroundFocused = JewelTheme.treeStyle.colors.elementBackgroundFocused, //JewelTheme.treeStyle.colors.elementBackgroundFocused
                elementBackgroundSelected = JewelTheme.treeStyle.colors.elementBackgroundSelected,
                elementBackgroundSelectedFocused = JewelTheme.treeStyle.colors.elementBackgroundSelectedFocused,
                content = JewelTheme.treeStyle.colors.content,
                contentFocused = JewelTheme.treeStyle.colors.contentFocused,
                contentSelected = JewelTheme.treeStyle.colors.contentSelected,
                contentSelectedFocused = JewelTheme.treeStyle.colors.contentSelectedFocused
            ),
            LazyTreeMetrics(
                JewelTheme.treeStyle.metrics.indentSize,
                JewelTheme.treeStyle.metrics.elementBackgroundCornerSize,
                PaddingValues(0.dp), //JewelTheme.treeStyle.metrics.elementPadding
                PaddingValues(horizontal = 4.dp, vertical = 4.dp), //JewelTheme.treeStyle.metrics.elementContentPadding
                JewelTheme.treeStyle.metrics.elementMinHeight,
                JewelTheme.treeStyle.metrics.chevronContentGap
            ),
            JewelTheme.treeStyle.icons
        )
    }
