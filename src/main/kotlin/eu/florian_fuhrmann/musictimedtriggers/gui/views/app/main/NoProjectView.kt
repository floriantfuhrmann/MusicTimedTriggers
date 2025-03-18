package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text


@Composable
fun NoProjectView() {
    Column(
        modifier = Modifier.fillMaxSize().background(JewelTheme.globalColors.panelBackground).padding(20.dp)
    ) {
        Row {
            Text("Please open or create a project to continue", fontSize = 1.em)
        }
        Row(Modifier.padding(top = 5.dp)) {
            DefaultButton(
                onClick = {
                    //todo
                }
            ) {
                Text("Open Project")
            }
            DefaultButton(
                modifier = Modifier.padding(start = 5.dp),
                onClick = {
                    //todo
                }
            ) {
                Text("Create Project")
            }
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(JewelTheme.globalColors.panelBackground)
                    .border(
                        width = 2.dp,
                        color = JewelTheme.globalColors.borders.disabled,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 20.dp)
                    .fillMaxSize(0.7f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Or Drag Project Directory here",
                    fontSize = 1.5.em,
                    fontWeight = FontWeight.Bold,
                    color = JewelTheme.globalColors.borders.disabled,
                    lineHeight = 1.0.em
                )
            }
        }
    }
}