package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text

@Composable
fun DialogFrame(
    leftButtons: @Composable () -> Unit = {},
    rightButtons: @Composable () -> Unit = {},
    dialogContent: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .background(JewelTheme.globalColors.paneBackground)
            .fillMaxSize()
            .padding(5.dp)
            .trackActivation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .trackActivation(),
            verticalArrangement = Arrangement.Bottom
        ) {
            //Content
            dialogContent()
            //Spacer to ensure the buttons are at the bottom
            Spacer(modifier = Modifier.weight(1f))
            //Buttons
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                //Left Buttons
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        leftButtons()
                    }
                }
                //Spacer in the middle
                Spacer(modifier = Modifier.weight(1f))
                //Right Buttons
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        rightButtons()
                    }
                }
            }
        }
    }
}

@Composable
fun CloseDialogButton(text: String = "Close", modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = {
            DialogManager.closeDialog()
        },
        modifier = modifier.trackActivation()
    ) {
        Text(text)
    }
}