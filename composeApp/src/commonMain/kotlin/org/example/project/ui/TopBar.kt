package org.example.project.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DrawerValue
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun getTopAppBar() {
    TopAppBar(
        title = {
            Text("Top Application Bar")
        },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon()
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = Color.White,
        elevation = 25.dp
    )
}

@Composable
fun getContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Magenta),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = "Content of the page",
            fontSize = 45.sp,
            color = Color.Green
        )
    }
}


// Creating a composable function to display Top Bar
@Composable
fun ScaffoldSample(){
    val scaffoldState = rememberScaffoldState(
        rememberDrawerState(DrawerValue.Closed)
    )

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { getTopAppBar() },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Text(text = "FAB example")
            }
        },
        drawerContent = { Text(text = "Drawer Menu 1")},
        content = { getContent() },
        bottomBar = {
            BottomAppBar (backgroundColor =MaterialTheme.colors.primary) {
                Text("Bottom App Bar")
            }
        }
    )
}

// Creating a composable function to create a simple card
// Calling this function as content in the above function
@Composable
fun MyContent(){
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Card{
            Column(Modifier.padding(10.dp)) {
                Text("GeeksforGeeks", fontWeight = FontWeight.W700)
                Text("+91 1800 2584458")
                Text("Noida, India", color = Color.Gray)
            }
        }
    }
}