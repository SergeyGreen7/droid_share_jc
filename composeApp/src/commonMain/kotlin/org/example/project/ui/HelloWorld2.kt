package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*

private val countryList =
    mutableListOf(
        "AAA","BBB","CCC","DDD","EEE","FFF","GGG","HHH","KKK",
        "AAA","BBB","CCC","DDD","EEE","FFF","GGG","HHH","KKK",
        "AAA","BBB","CCC","DDD","EEE","FFF","GGG","HHH","KKK",
        "AAA","BBB","CCC","DDD","EEE","FFF","GGG","HHH","KKK")

private val listModifier = Modifier
    .fillMaxSize()
    .background(Color.Gray)
    .padding(15.dp)

private val textStyle = TextStyle(
    fontSize = 20.sp,
    color = Color.Black
)

@Composable
fun lazyListSample() {
    LazyColumn {
        item {
            Text(text = "Sample text")
        }

        items(3) { index ->
            Text(text = "First list items: $index")
        }

        items(2) { index ->
            Text(text = "Second list items: $index")
        }
        item {
            Text(text = "Footer")
        }
    }
    simpleListView()
}

@Composable
fun simpleListView() {
    LazyColumn(modifier = listModifier) {
        items(countryList) { country ->
            Text(text  = country, style = textStyle)
        }
    }
}