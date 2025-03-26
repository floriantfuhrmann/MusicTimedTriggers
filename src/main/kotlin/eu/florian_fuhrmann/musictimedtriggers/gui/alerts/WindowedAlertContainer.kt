package eu.florian_fuhrmann.musictimedtriggers.gui.alerts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.jetbrains.JBR
import java.awt.event.MouseEvent

val uiScale = System.getProperty("sun.java2d.uiScale").toDoubleOrNull() ?: 1.0

fun FrameWindowScope.getCenteredAbsoluteWindowPosition(density: Density, width: Dp, height: Dp): WindowPosition {
    return with(density) {
        WindowPosition.Absolute(
            x = uiScale * window.locationOnScreen.x.toDp() + uiScale * window.size.width.toDp() / 2 - width / 2,
            y = uiScale * window.locationOnScreen.y.toDp() + uiScale * window.size.height.toDp() / 2 - height / 2
        )
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun WindowedAlertContainer(frameWindowScope: FrameWindowScope, content: @Composable () -> Unit) {
    // remember local density for later
    val localDensity = LocalDensity.current
    // init dialog state in top right corner with initial size
    val initialAlertWidth = 370.dp
    val initialAlertHeight = 500.dp
    val dialogState = rememberDialogState(
        position = WindowPosition.Absolute(0.dp, 0.dp),
        width = initialAlertWidth,
        height = initialAlertHeight
    )
    // states for when the dialog is globally positioned and when the content should become visible
    var globallyPositioned by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var alpha by remember { mutableStateOf(0.0f) }
    // create the dialog window
    DialogWindow(
        state = dialogState,
        onCloseRequest = {},
        visible = true,
        title = "Windowed Alert Container",
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = true,
        alwaysOnTop = true
    ) {
        Box(
            Modifier
//                .alpha(if(contentVisible) 1.0f else 0.0f)
                .alpha(alpha)
                .onPointerEvent(PointerEventType.Press, PointerEventPass.Main) {
                    if (this.currentEvent.button == PointerButton.Primary && this.currentEvent.changes.any { changed -> !changed.isConsumed }) {
                        JBR.getWindowMove().startMovingTogetherWithMouse(window, MouseEvent.BUTTON1)
                    }
                }
                .onGloballyPositioned { coordinates ->
                    with(localDensity) {
                        println("onGloballyPositioned: ${coordinates.size.width.toDp()} x ${coordinates.size.height.toDp()}")
                        dialogState.size = DpSize(coordinates.size.width.toDp(), coordinates.size.height.toDp())
                        dialogState.position = frameWindowScope.getCenteredAbsoluteWindowPosition(
                            localDensity,
                            dialogState.size.width,
                            dialogState.size.height
                        )
                        globallyPositioned = true
                    }
                }
        ) {
            AnimatedVisibility(
                visible = contentVisible,
                enter = scaleIn() + fadeIn(),
            ) {
                content.invoke()
            }
            // if the content should not be visible display the content normally (will be hidden through alpha on container box)
            if(!contentVisible) {
                content.invoke()
            }
            LaunchedEffect(globallyPositioned) {
                if (globallyPositioned) {
//                    println("Starting short delay")
//                    delay(5)
                    println("Setting content visible")
                    contentVisible = true
                    alpha = 1.0f
                }
            }
        }

    }
}