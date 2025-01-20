package org.example.project.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random

var numbers = mutableStateListOf<Int>(10, 20, 30)

@Composable
fun getButtons() {
    getNumChanger(
        numbers,
        onPlusPress = { ->
            numbers.add(Random.nextInt()  % 1000)
            println("numbers size = ${numbers.size}")
        },
        onMinusPress = { ->
            if (numbers.isNotEmpty()) {
                numbers.removeLast()
            }
            println("numbers size = ${numbers.size}")
        }
    )
}

@Composable
fun getView() {
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){

        var numbers = remember { numbers }

        getGrid(numbers)
        Spacer(modifier = Modifier.weight(1f))
        getButtons()
    }
}

@Composable
fun getGrid(numbers: SnapshotStateList<Int>) {
   LazyVerticalGrid(
       columns = GridCells.Fixed(3)
   ) {
       items(numbers.size) { index ->
           Row {
               Button(onClick = {}) {
                   Text("Button")
               }
               Spacer(modifier = Modifier.width(10.dp))
               Text("Some text with number: ${numbers[index]}")
           }
       }
   }
}

@Composable
fun getNumChanger(
    numbers: SnapshotStateList<Int>,
    onPlusPress: () -> Unit,
    onMinusPress: () -> Unit,
) {
    Row {
        Text(
            text = "Current number = ${numbers.size}"
        )
        Spacer(modifier = Modifier.width(15.dp))
        Button(onClick = {
            onPlusPress.invoke()
        }) {
            Text("Increase number")
        }
        Spacer(modifier = Modifier.width(15.dp))
        Button(onClick = {
            onMinusPress.invoke()
        }) {
            Text("Decrease number")
        }
    }
}

