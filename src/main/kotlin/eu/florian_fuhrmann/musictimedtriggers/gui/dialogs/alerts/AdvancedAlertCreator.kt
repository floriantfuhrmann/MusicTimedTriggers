package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.alerts

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager.closeAlert
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggersManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text

object AdvancedAlertCreator {

    fun createUsagesAlert(templates: List<AbstractTriggerTemplate>, usages: List<TriggersManager.TriggerUsage>): AdvancedAlert {
        return AdvancedAlert(
            onDismissRequest = { closeAlert() },
            title = {
                Text(when(usages.size) {
                    0 -> if(templates.size == 1) "The template is not used in any placed triggers." else "The templates are not used in any placed triggers."
                    1 -> if(templates.size == 1) "The template is still used one time" else "One template is still used in one place"
                    else -> if(templates.size == 1) "The template is still used ${usages.size} times" else "The templates are still used ${usages.size} times"
                }, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(when (usages.size) {
                        0 -> buildAnnotatedString {
                            append("Continuing with deletion will not delete any placed triggers.")
                        }

                        1 -> buildAnnotatedString {
                            append("Continuing with deletion will also remove ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("one")
                            }
                            append(" placed trigger:")
                        }

                        else -> buildAnnotatedString {
                            append("Continuing with deletion will also remove ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("${usages.size}")
                            }
                            append(" placed triggers:")
                        }
                    }
                )
                //Todo: list usages
            },
            dismissButton = {
                OutlinedButton(
                    modifier = Modifier.padding(bottom = 5.dp),
                    onClick = { closeAlert() }
                ) {
                    Text("Abort")
                }
            },
            confirmButton = {
                OutlinedButton(
                    modifier = Modifier.padding(bottom = 5.dp),
                    onClick = { closeAlert() }
                ) {
                    Text("Confirm deletion", color = Color.Red)
                }
            }
        )
    }

}