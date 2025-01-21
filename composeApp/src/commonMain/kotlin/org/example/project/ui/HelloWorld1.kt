package org.example.project.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun HelloWorld() {

    var count by remember { mutableStateOf(0) }

    var big by remember { mutableStateOf(false) }
    val size by animateDpAsState(if (big) 100.dp else 50.dp)

    Column(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                text = "Hello, Jetpack Compose!",
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.LightGray),
                color = Color.Red,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("  Welcome to Jetpack Compose!")

        }
        Text("Count: $count")
        Row {
            Button(onClick = { count++ }) {
                Text("Increase count")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = { count-- }) {
                Text("Decrease count")
            }
        }
        Spacer(modifier = Modifier
            .width(10.dp)
            .height(10.dp)
        )
        Box(
            modifier = Modifier
                .size(size)
                .background(Color.Red)
                .clickable { big = !big }
        )
        Spacer(modifier = Modifier.height(25.dp))
        SimpleRow()
    }
}

@Composable
fun SimpleRow() {
    val spacer = Spacer(modifier = Modifier.width(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement  =  Arrangement.SpaceEvenly
    ) {
        Text(text = "Row Text 1", Modifier.background(Color.Red))
        simpleSpacer()
        Text(text = "Row Text 2", Modifier.background(Color.White))
        simpleSpacer()
        Text(text = "Row Text 3", Modifier.background(Color.Green))
        simpleSpacer()
        TextWithSize("123123123123123")
    }

    Row (
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Green
            ),
            shape = AbsoluteRoundedCornerShape(10.dp)
        ) {
            Column{
                Text(
                    text = "Button1",
                    color = Color.Red)
                Text(
                    text = "Button1 again",
                    color = Color.Red)
            }

        }
        simpleSpacer()
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Yellow
            )
        ) {
            Text(
                text = "Button1",
                color = Color.Red)
        }
        simpleSpacer()
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Magenta
            ),
            shape = RectangleShape
        ) {
            Text(
                text = "Button3",
                color = Color.White)
        }
        simpleSpacer()
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Magenta
            ),
            shape = RectangleShape,
            border = BorderStroke(3.dp, Color.Red)
        ) {
            Text(
                text = "Button3",
                color = Color.White)
        }
        simpleSpacer()
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Magenta
            ),
            shape = RectangleShape,
            border = BorderStroke(3.dp, Color.Red),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 10.dp,
                pressedElevation = 20.dp,
                disabledElevation = 0.dp
            )
        ) {
            Text(
                text = "Button4",
                color = Color.White)
        }
    }
}

@Composable
fun simpleSpacer() {
    Spacer(modifier = Modifier.width(10.dp))
}

@Composable
fun TextWithSize(label : String) {
    Text(label)
}

