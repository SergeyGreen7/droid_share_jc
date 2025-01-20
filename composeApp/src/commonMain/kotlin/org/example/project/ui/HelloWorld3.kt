package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*

data class TestModel(
    val text: String,
    var id: Int
)

@Composable
fun ListRow(model: TestModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(Color.LightGray)
    ) {
        Button(
            onClick = { model.id++ }
        ) {
            Text(text = "Data is: ${model.id}")
        }
        Text(text = "Mode.text = ${model.text}: ${model.id}")
    }
}

private val models =
    mutableListOf(
        TestModel("AAA",12),
        TestModel("BBB",13),
        TestModel("CCC",14),
        TestModel("DDD",15))

@Composable
fun getLazyColumnTestModel() {
    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(models) { model ->
            ListRow(model)
        }
    }

}