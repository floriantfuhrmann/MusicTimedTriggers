package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.triggerusages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggersManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.theme.linkStyle

class TriggerUsagesDialog(
    val type: Type,
    val usages: List<TriggersManager.TriggerUsage>,
    val onConfirm: (() -> Unit)? = null
) : Dialog(
    title = when (type) {
        Type.ViewTemplateUsages -> "Trigger Template Usages"
        Type.DeleteTemplates -> "Delete Trigger Templates"
        Type.ShortenSequenceByReplacing -> "Shorten Sequence by Replacing Audio"
    }
) {
    @Composable
    override fun Content() {
        // Focus Requesters for cancel button
        val cancelFocusRequester = remember { FocusRequester() }
        // Content
        Column(modifier = Modifier.background(JewelTheme.globalColors.panelBackground).padding(10.dp).fillMaxSize()) {
            // Message (Top Content)
            Column {
                if(type == Type.DeleteTemplates) {
                    Row(Modifier.padding(bottom = 10.dp)) {
                        Text(buildAnnotatedString {
                            append("Continuing with deleting will also remove ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(if (usages.size == 1) "one" else "${usages.size}")
                            }
                            append(" placed trigger${if(usages.size == 1) "" else "s"}:")
                        })
                    }
                } else if(type == Type.ShortenSequenceByReplacing) {
                    Row(Modifier.padding(bottom = 10.dp)) {
                        Text(buildAnnotatedString {
                            append("Continuing with shortening the sequence by replacing the audio will also remove ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(if (usages.size == 1) "one" else "${usages.size}")
                            }
                            append(" protruding placed trigger${if(usages.size == 1) "" else "s"}:")
                        })
                    }
                }
            }
            // Usages (Middle Content)
            Column(Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, JewelTheme.globalColors.borders.normal)
            ) {
                VerticallyScrollableContainer {
                    Column {
                        Spacer(Modifier.height(4.dp))
                        for(usage in usages) {
                            if(usage != usages.first()) {
                                Divider(Orientation.Horizontal, Modifier.fillMaxWidth().padding(vertical = 4.dp))
                            }
                            Text(buildAnnotatedString {
                                withStyle(SpanStyle(color = JewelTheme.linkStyle.colors.contentDisabled)) {
                                    append("${usage.song.name} > ${usage.line.name} > ")
                                }
                                withLink(
                                    LinkAnnotation.Clickable(
                                        "",
                                        TextLinkStyles(style = SpanStyle(color = JewelTheme.linkStyle.colors.content)),
                                        linkInteractionListener = {
                                            // close dialog and open song at time
                                            DialogManager.closeDialog()
                                            ProjectManager.currentProject?.openSongAtTime(usage.song, usage.placedTrigger.startTime)
                                        }
                                    )
                                ) {
                                    append("${usage.placedTrigger.name()} (${formatSeconds(usage.placedTrigger.startTime)})")
                                }
                            }, Modifier.padding(horizontal = 4.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
            // Buttons (Bottom Content)
            Column {
                Row(Modifier.padding(top = 10.dp)) {
                    // Spacer
                    Spacer(Modifier.weight(1f))
                    // Close button
                    CloseDialogButton(when(type) {
                        Type.ViewTemplateUsages -> "Close"
                        Type.DeleteTemplates, Type.ShortenSequenceByReplacing -> "Cancel"
                    }, Modifier.focusRequester(cancelFocusRequester))
                    // Delete button
                    if (type == Type.DeleteTemplates || type == Type.ShortenSequenceByReplacing) {
                        DefaultButton(
                            modifier = Modifier.padding(start = 12.dp),
                            onClick = {
                                // close dialog and invoke onConfirm
                                DialogManager.closeDialog()
                                onConfirm?.invoke()
                            }
                        ) {
                            Text(when(type) {
                                Type.DeleteTemplates -> "Delete"
                                Type.ShortenSequenceByReplacing -> "Delete and Replace"
                                Type.ViewTemplateUsages -> throw IllegalStateException()
                            })
                        }
                    }
                }
            }
        }
        // Request Focus for cancel button
        if(type != Type.ViewTemplateUsages) {
            LaunchedEffect(Unit) {
                cancelFocusRequester.requestFocus()
            }
        }
    }

    private fun formatSeconds(seconds: Double): String {
        val m = seconds.toInt() / 60
        val s = seconds.toInt() % 60
        return "${if(m<10){"0"}else{""}}$m:${if(s<10){"0"}else{""}}$s"
    }

    enum class Type {
        ViewTemplateUsages,
        DeleteTemplates,
        ShortenSequenceByReplacing
    }
}