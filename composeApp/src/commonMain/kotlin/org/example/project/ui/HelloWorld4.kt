package org.example.project.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

class Test : ViewModel() {
    var number = mutableStateOf(0)
}

@Composable
fun helloWorld4() {
    var count by remember { mutableStateOf(0) }
    var list = remember { mutableStateListOf<Int>() }

    Column {
        Button(
            onClick = {
                count++
                list.add(count)
            }
        ) {
            Text("Increase count")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("count is $count")
        Spacer(modifier = Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3)
        ) {
            items(list.size) { index ->
                Row {
                    Button(onClick = {}) {
                        Text("Button")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Some text with number: ${list[index]}")
                }
            }
        }
    }
}