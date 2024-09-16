package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text

class Alert(
    val title: String = "Alert",
    val text: String,
    val onDismiss: (() -> Unit)? = null,
    val dismissText: String = "Dismiss",
    val onConfirm: (() -> Unit)? = null,
    val confirmText: String = "Confirm"
) {

    @Composable
    fun Content(totalAlertAmount: Int = 1) {
        AlertDialog(
            onDismissRequest = {
                DialogManager.closeAlert()
                onDismiss?.invoke()
            },
            title = {
                Text(
                    fontStyle = JewelTheme.defaultTextStyle.fontStyle,
                    fontWeight = FontWeight.Bold,
                    text = if (totalAlertAmount > 1) {
                        "($totalAlertAmount) "
                    } else {
                        ""
                    } + title
                )
            },
            text = {
                Text(fontStyle = JewelTheme.defaultTextStyle.fontStyle, text = text)
            },
            backgroundColor = JewelTheme.globalColors.paneBackground,
            properties = DialogProperties(
                dismissOnClickOutside = true
            ),
            confirmButton = {
                if(onConfirm != null) {
                    DefaultButton(
                        onClick = {
                            DialogManager.closeAlert()
                            onConfirm.invoke()
                        },
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                            .trackActivation()
                    ) {
                        Text(confirmText)
                    }
                }
            },
            dismissButton = {
                if(onDismiss != null) {
                    OutlinedButton(
                        onClick = {
                            DialogManager.closeAlert()
                            onDismiss.invoke()
                        },
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                            .trackActivation()
                    ) {
                        Text(dismissText)
                    }
                }
            }
        )
    }

}