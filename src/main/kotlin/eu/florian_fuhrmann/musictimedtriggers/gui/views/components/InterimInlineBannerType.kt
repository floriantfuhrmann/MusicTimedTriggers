package eu.florian_fuhrmann.musictimedtriggers.gui.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sun.rowset.internal.Row
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.colorPalette

/**
 * Inline Banner similar to the one from Jewel after Jewel Version 0.27.0.
 * Should only be used intermediately until Inline Banner is available in
 * Jewel / newer Jewel Version is released.
 */
@Deprecated("This is a temporary solution until Inline Banner is available in Jewel.")
@Composable
fun InterimInlineBanner(
    type: InterimInlineBannerType = InterimInlineBannerType.Info,
    modifier: Modifier = Modifier,
    text: String,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    val backgroundColor = when (type) {
        InterimInlineBannerType.Info -> JewelTheme.colorPalette.blue(1)
        InterimInlineBannerType.Success -> JewelTheme.colorPalette.green(1)
        InterimInlineBannerType.Warning -> JewelTheme.colorPalette.yellow(1)
        InterimInlineBannerType.Error -> JewelTheme.colorPalette.red(1)
    }
    val borderColor = when (type) {
        InterimInlineBannerType.Info -> JewelTheme.colorPalette.blue(3)
        InterimInlineBannerType.Success -> JewelTheme.colorPalette.green(2)
        InterimInlineBannerType.Warning -> JewelTheme.colorPalette.yellow(2)
        InterimInlineBannerType.Error -> JewelTheme.colorPalette.red(2)
    }
    Box(
        modifier
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        Row(Modifier.padding(12.dp)) {
            Column(Modifier.padding(end = 8.dp)) {
                Icon(AllIconsKeys.General.BalloonError, null)
            }
            Column {
                Text(text)
                if(actions != null) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        actions()
                    }
                }
            }
        }
    }
}

enum class InterimInlineBannerType {
    Info,
    Success,
    Warning,
    Error
}