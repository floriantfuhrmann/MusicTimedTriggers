package eu.florian_fuhrmann.musictimedtriggers.gui.alerts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import com.jetbrains.JBR
import org.jetbrains.jewel.ui.util.thenIf
import java.awt.Point
import java.awt.Window
import java.awt.event.MouseEvent
import kotlin.math.roundToInt

@Composable
fun WindowedAlertContainer(dialogWindowScope: DialogWindowScope, content: @Composable () -> Unit) {
    WindowedAlertContainer(with(dialogWindowScope) {window}, content)
}

@Composable
fun WindowedAlertContainer(frameWindowScope: FrameWindowScope, content: @Composable () -> Unit) {
    WindowedAlertContainer(with(frameWindowScope) {window}, content)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun WindowedAlertContainer(parentAwtWindow: Window, content: @Composable () -> Unit) {
    // remember local density for later
    val localDensity = LocalDensity.current
    // states for when the dialog is globally positioned and when the content should become visible
    var globallyPositioned by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var alpha by remember { mutableStateOf(0.0f) }
    // the child awt window for positioning
    var childAwtWindow: Window
    // create the dialog window
    DialogWindow(
        state = DialogState(WindowPosition.Absolute(0.dp, 0.dp), 370.dp, 500.dp),
        onCloseRequest = {},
        visible = true,
        title = "Windowed Alert Container",
        undecorated = true,
        transparent = true,
        resizable = false,
        focusable = true,
        alwaysOnTop = true
    ) {
        childAwtWindow = this.window
        Box(
            Modifier
//                .alpha(if(contentVisible) 1.0f else 0.0f)
                .alpha(alpha)
                .thenIf(JBR.isWindowMoveSupported()) {
                    onPointerEvent(PointerEventType.Press, PointerEventPass.Main) {
                        if (this.currentEvent.button == PointerButton.Primary && this.currentEvent.changes.any { changed -> !changed.isConsumed }) {
                            JBR.getWindowMove().startMovingTogetherWithMouse(window, MouseEvent.BUTTON1)
                        }
                    }
                }
                .thenIf(!JBR.isWindowMoveSupported()) {
                    var mouseDownPoint: Point? by remember { mutableStateOf(null) }
                    onPointerEvent(PointerEventType.Press, PointerEventPass.Main) {
                        it.nativeEvent.let { event ->
                            if (event !is MouseEvent) return@onPointerEvent
                            if (event.button != MouseEvent.BUTTON1) return@onPointerEvent
                            mouseDownPoint = event.point
                        }
                    }
                    .onPointerEvent(PointerEventType.Move, PointerEventPass.Main) {
                        it.nativeEvent.let { event ->
                            if (event !is MouseEvent) return@onPointerEvent
                            mouseDownPoint.let { mouseDownPoint ->
                                if (mouseDownPoint == null) return@onPointerEvent
                                childAwtWindow.setLocation(event.locationOnScreen.x - mouseDownPoint.x, event.locationOnScreen.y - mouseDownPoint.y)
                            }
                        }
                    }
                    .onPointerEvent(PointerEventType.Release, PointerEventPass.Main) {
                        mouseDownPoint = null
                    }
                    .onPointerEvent(PointerEventType.Exit, PointerEventPass.Main) {
                        mouseDownPoint = null
                    }
                }
                .onGloballyPositioned { coordinates ->
                    with(localDensity) {
                        childAwtWindow.setBounds(0, 0, coordinates.size.width.toDp().value.roundToInt(), coordinates.size.height.toDp().value.roundToInt())
                        childAwtWindow.setLocationRelativeTo(parentAwtWindow)
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
                    contentVisible = true
                    alpha = 1.0f
                }
            }
        }
    }
}