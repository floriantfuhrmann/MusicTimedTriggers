package eu.florian_fuhrmann.musictimedtriggers.gui.styles

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.FixedCursorPoint
import org.jetbrains.jewel.ui.component.styling.TooltipMetrics
import org.jetbrains.jewel.ui.component.styling.TooltipStyle
import org.jetbrains.jewel.ui.theme.tooltipStyle
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class)
val cursorTooltipStyle: TooltipStyle
    @Composable
    get() = TooltipStyle(
        colors = JewelTheme.tooltipStyle.colors,
        metrics = TooltipMetrics(
            contentPadding = JewelTheme.tooltipStyle.metrics.contentPadding,
            showDelay = 400.milliseconds,
            cornerSize = JewelTheme.tooltipStyle.metrics.cornerSize,
            borderWidth = JewelTheme.tooltipStyle.metrics.borderWidth,
            shadowSize = JewelTheme.tooltipStyle.metrics.shadowSize,
            placement = TooltipPlacement.CursorPoint(
                offset = DpOffset(0.dp, 8.dp),
                alignment = Alignment.BottomStart,
                windowMargin = 0.dp
            )
        )
    )

@OptIn(ExperimentalFoundationApi::class, ExperimentalJewelApi::class)
val fixedCursorTooltipStyle: TooltipStyle
    @Composable
    get() = TooltipStyle(
        colors = JewelTheme.tooltipStyle.colors,
        metrics = TooltipMetrics(
            contentPadding = JewelTheme.tooltipStyle.metrics.contentPadding,
            showDelay = 500.milliseconds,
            cornerSize = JewelTheme.tooltipStyle.metrics.cornerSize,
            borderWidth = JewelTheme.tooltipStyle.metrics.borderWidth,
            shadowSize = JewelTheme.tooltipStyle.metrics.shadowSize,
            placement = FixedCursorPoint(
                offset = DpOffset(0.dp, 16.dp),
                alignment = Alignment.BottomEnd,
                windowMargin = 0.dp
            )
        )
    )