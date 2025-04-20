package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
@Preview
fun Experiment() {
    IntUiTheme {
        Row(Modifier.fillMaxSize()) {
            // BEGIN UNCHANGEABLE
            Column(Modifier.weight(1f)) {
                Row {
                    Column(Modifier.weight(1f).fillMaxHeight().background(Color.Blue)) {
                        Row(
                            modifier = Modifier
                                .background(JewelTheme.globalColors.borders.normal)
                                .padding(top = 1.dp)
                                .background(JewelTheme.globalColors.panelBackground)
                                .padding(horizontal = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Hello")
                            }
                            Spacer(Modifier.weight(1f))
                            Column {
                                Text("World")
                            }
                        }
                    }
                }
            }
            // END UNCHANGEABLE
            Column(Modifier.width(IntrinsicSize.Min).fillMaxHeight().background(Color.Red)) {
                Text("Foobar")
            }
        }
    }
}