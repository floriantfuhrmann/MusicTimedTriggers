package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager.closeAlert
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggersManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import org.jetbrains.jewel.foundation.Stroke
import org.jetbrains.jewel.foundation.modifier.border
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.component.styling.LocalLinkStyle

object AlertCreator {

    private fun formatSeconds(seconds: Double): String {
        val m = seconds.toInt() / 60
        val s = seconds.toInt() % 60
        return "${if(m<10){"0"}else{""}}$m:${if(s<10){"0"}else{""}}$s"
    }

    fun createUsagesAlert(
        deleteMode: Boolean, // whether usages are displayed for deletion or just for information
        templates: List<AbstractTriggerTemplate>,
        usages: List<TriggersManager.TriggerUsage>,
        onConfirm: (() -> Unit)? = null // only needed if deleteMode is true
    ): CustomAlert {
        return CustomAlert(
            onDismissRequest = { closeAlert() },
            content = {
                val disabledTextStyle = SpanStyle(
                    color = LocalLinkStyle.current.colors.contentDisabled,
                )
                val linkStyle = SpanStyle(
                    color = LocalLinkStyle.current.colors.content,
                    textDecoration = TextDecoration.Underline
                )
                Column {
                    // Title
                    Row(modifier = Modifier.padding(top = 15.dp, start = 15.dp, end = 15.dp)) {
                        Text(
                            if (deleteMode) {
                                when (usages.size) {
                                    0 -> if (templates.size == 1) "The template is not used in any placed triggers" else "The templates are not used in any placed triggers"
                                    1 -> if (templates.size == 1) "The template is still used one time" else "One template is still used in one place"
                                    else -> if (templates.size == 1) "The template is still used ${usages.size} times" else "The templates are still used ${usages.size} times"
                                }
                            } else {
                                "Template Usages"
                            }, fontWeight = FontWeight.Bold, color = if (usages.isEmpty() || !deleteMode) JewelTheme.globalColors.text.normal else Color.Red
                        )
                    }
                    // Message
                    Row(modifier = Modifier.padding(top = 8.dp, start = 15.dp, end = 15.dp)) {
                        if (deleteMode) {
                            Text(
                                when (usages.size) {
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
                            })
                        } else if (usages.isEmpty()) {
                            Text("No usages found")
                        }
                        //else: in non-delete mode, this row will just serve as a spacer
                    }
                    // Usages
                    if(usages.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(top = 1.dp, start = 15.dp, end = 15.dp)
                                .border(Stroke.Alignment.Outside, 1.dp, JewelTheme.globalColors.borders.normal)
                        ) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight(0.5f)) {
                                val scrollState = rememberScrollState()
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top)
                                    ) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        for (usage in usages) {
                                            Row {
                                                val text = buildAnnotatedString {
                                                    // song and line name (with fake link, because pressing space counts
                                                    // as a click on the first clickable link and I couldn't find a
                                                    // better way to disable this behavior other than adding a fake link
                                                    // as the first one)
                                                    withLink(
                                                        LinkAnnotation.Clickable(
                                                            tag = "song_and_line_name",
                                                            linkInteractionListener = { })
                                                    ) {
                                                        withStyle(disabledTextStyle) {
                                                            append("${usage.song.name} > ${usage.line.name} > ")
                                                        }
                                                    }
                                                    // actual link to goto placed trigger
                                                    withLink(
                                                        LinkAnnotation.Clickable(
                                                            tag = "placed_trigger_name",
                                                            linkInteractionListener = { _ ->
                                                                closeAlert()
                                                                ProjectManager.currentProject?.openSongAtTime(
                                                                    usage.song,
                                                                    usage.placedTrigger.startTime
                                                                )
                                                            }
                                                        )) {
                                                        withStyle(linkStyle) {
                                                            append("${usage.placedTrigger.name()} (${formatSeconds(usage.placedTrigger.startTime)})")
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = text,
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                        .padding(end = scrollbarContentSafePadding())
                                                )
                                            }
                                            if (usage != usages.last()) {
                                                Row {
                                                    Box(
                                                        Modifier.height(8.dp),
                                                        contentAlignment = Alignment.CenterStart
                                                    ) {
                                                        Divider(Orientation.Horizontal, Modifier.fillMaxWidth())
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                                VerticalScrollbar(
                                    scrollState = scrollState,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                )
                            }
                        }
                    }
                    // Buttons
                    Row(modifier = Modifier.padding(top = 15.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            modifier = Modifier,
                            onClick = { closeAlert() }
                        ) {
                            Text(if (deleteMode) "Abort" else "Close")
                        }
                        if(deleteMode) {
                            OutlinedButton(
                                modifier = Modifier.padding(start = 5.dp),
                                onClick = {
                                    closeAlert()
                                    onConfirm!!()
                                }
                            ) {
                                Text("Confirm Deletion", color = Color.Red)
                            }
                        }
                    }
                }
            }
        )
    }

}