package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode

@Composable
fun FileShareManiUI(
    vm: FileShareViewModel = viewModel(),
) {
    DialogWindow(vm)
    ProgressWindow(vm)
    PairingWindow(vm)

    Column {
        GetGrid(vm)
        Spacer(modifier = Modifier.weight(1f))
        GetButtons(vm)
    }

    NotificationWindow(vm)
}

@Composable
fun GetButtons(
    vm: FileShareViewModel,
) {
    val showDebugButtons = false

    val filePicker = rememberFilePickerLauncher(mode = PickerMode.Multiple()) { files ->
        println("launcher, files = $files")
        vm.createFileDscrList(files)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Row {
            Button(
                onClick = {
                    filePicker.launch()
                },
            ) {
                Text(text = "Select file")
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                onClick = {
                    vm.sendDataCallback()
                },
                enabled = vm.sendDataButtonIsActive.value
            ) {
                Text(text = "Send Data")
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(
                onClick = {
                    vm.pairCreationCallback()
                },
            ) {
                Text(text = "Make Pair")
            }
        }

        FuncCheckUI(vm, showDebugButtons)

        Spacer(modifier = Modifier.width(15.dp))
        Row {
            Text(
                text = vm.nameStr.value,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = vm.fileStr.value,
            )
        }
        Spacer(modifier = Modifier.width(15.dp))
    }
}

@Composable
fun GetGrid(
    vm: FileShareViewModel,
) {
    vm.checkSelectedIndexForDrop()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
    ) {
        itemsIndexed(vm.discoveredDeviceList) { index, item ->
            Column(
                modifier = Modifier
                    .selectable(
                        selected = vm.isIndexSelected(index),
                        onClick = {
                            vm.setSelectedIndex(index)
                            vm.setDeviceInfoCallback.invoke(vm.discoveredDeviceList[index], index)
                        }
                    )
                    .background(
                        color = if (vm.isIndexSelected(index)) {
                            Color.Green
                        } else {
                            Color.White
                        }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(15.dp)
                        .align(alignment = Alignment.CenterHorizontally)
                ) {
                    Text("Name: ${item.deviceName}")
                    Text("Info: ${item.deviceInfo}")
                }
            }
        }
    }
}
