package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer

/**
 * Container intended for inspector contents. Consists of a column with
 * a title and a box, which is not scrollable itself, but [contents] is
 * intended to include a scrollable container (for example under a banner).
 */
@Composable
fun InspectorContentsContainer(title: String, scrollState: ScrollState = rememberScrollState(), contents: @Composable BoxScope.() -> Unit) {
    Column(
        Modifier
            .background(JewelTheme.globalColors.borders.normal)
            .padding(start = 1.dp)
            .background(JewelTheme.globalColors.panelBackground)
    ) {
        // Title
        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp).height(25.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
        }
        // Divider
        Divider(
            orientation = Orientation.Horizontal,
            modifier = Modifier.height(1.dp).fillMaxWidth(),
            color = if (scrollState.canScrollBackward) JewelTheme.globalColors.borders.normal else Color.Transparent
        )
        // Content
        Row {
            Box(Modifier.fillMaxSize()) {
                //Contents
                contents()
            }
        }
    }
}

/**
 * Container intended for inspector contents. Consists of a column with
 * a title and a box, which is scrollable.
 */
@Composable
fun ScrollableInspectorContentsContainer(title: String, contents: @Composable BoxScope.() -> Unit) {
    // Scroll State for the content
    val scrollState = rememberScrollState()
    // not scrollable Inspector Contents Container
    InspectorContentsContainer(title, scrollState) {
        // scrollable container
        VerticallyScrollableContainer(scrollState = scrollState) {
            // Contents
            contents()
        }
    }
}