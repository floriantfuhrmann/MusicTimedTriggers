package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text

open class AdvancedAlert(
    val onDismissRequest: () -> Unit /* = { DialogManager.closeAlert() }*/,
    val title: @Composable (() -> Unit)? = null,
    val text: @Composable (() -> Unit),
    val confirmButton: @Composable (() -> Unit) = {},
    val dismissButton: @Composable (() -> Unit)? = null,
) : AbstractAlert() {

    @Composable
    override fun Content(totalAlertAmount: Int) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = title,
            text = text,
            backgroundColor = JewelTheme.globalColors.panelBackground,
            properties = DialogProperties(dismissOnClickOutside = true),
            confirmButton = confirmButton,
            dismissButton = dismissButton
        )
    }

}

fun createExampleAdvancedAlert(): AdvancedAlert {
    val dismiss = {
        DialogManager.closeAlert()
    }
    return AdvancedAlert(
        onDismissRequest = dismiss,
        title = {
            Text("Title", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.background(color = Color.Blue)) {
                Text(
                    buildAnnotatedString {
                        append("This alert has a ")
                        withStyle(style = SpanStyle(color = Color.Red)) {
                            append("red")
                        }
                        append(" box.")
                    }
                )
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.Red)
                    )
                }
            }
        },
        confirmButton = {
            Row {
                DefaultButton(
                    modifier = Modifier.padding(bottom = 5.dp),
                    onClick = {
                        println("Confirmed")
                        dismiss()
                    }
                ) {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            Row {
                Column {
                    OutlinedButton(
                        modifier = Modifier.padding(bottom = 5.dp),
                        onClick = {
                            dismiss()
                        }
                    ) {
                        Text("Dismiss")
                    }
                }
                Column {
                    OutlinedButton(
                        modifier = Modifier.padding(start = 5.dp, bottom = 5.dp),
                        onClick = {}
                    ) {
                        Text("Free extra button", color = Color.Yellow)
                    }
                }
            }
        }
    )
}

fun createExampleAdvancedAlert2(): AdvancedAlert {
    return AdvancedAlert(
        onDismissRequest = { DialogManager.closeAlert() },
        text = {
            Column(modifier = Modifier.background(color = Color.Blue)) {
                Row {
                    Text(
                        buildAnnotatedString {
                            append("This alert also has a ")
                            withStyle(style = SpanStyle(color = Color.Red)) {
                                append("red")
                            }
                            append(" box.")
                        }
                    )
                }
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.Red)
                    )
                }
                Row {
                    Text("But this alert doesn't have a title or buttons.")
                }
            }
        }
    )
}