package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserTemplate
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.IconsDummy
import eu.florian_fuhrmann.musictimedtriggers.utils.color.getContrasting
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text

@Composable
fun Overlay() {
    //get browser state
    val browserState = ProjectManager.currentProject?.browserState ?: return
    //only show overlay when dragging
    if (!browserState.draggingSelectedTemplates.value || browserState.draggingOnTimeline.value) return
    Box(
        modifier = Modifier
            .zIndex(1f)
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .offset(browserState.pointerOffset.value.x.dp, browserState.pointerOffset.value.y.dp)
                .alpha(0.8f)
        ) {
            browserState.selectedTemplates.forEach { browserTemplate ->
                DraggedTemplate(browserTemplate)
            }
        }
    }
}

@Composable
private fun DraggedTemplate(browserTemplate: BrowserTemplate) {
    val backgroundColor = browserTemplate.composeColor.value
    val textColor = backgroundColor.getContrasting(Color.White, Color.Black)
    Row(
        modifier = Modifier
            .width(250.dp)
            .height(IntrinsicSize.Min)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(5.dp)
            )
            .border(
                width = 0.dp,
                color = Color.Black,
                shape = RoundedCornerShape(5.dp)
            )
            .padding(5.dp)
    ) {
        //Icon Column
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(end = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(
                    browserTemplate.type.iconResource,
                    null,
                    IconsDummy::class.java,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        //Name Column
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(color = textColor, text = browserTemplate.name.value)
            }
        }
    }
}