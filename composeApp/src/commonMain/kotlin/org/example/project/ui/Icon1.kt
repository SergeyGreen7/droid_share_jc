package org.example.project.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun Icon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(100.dp), onDraw = {

        val canvasWidth = size.width
        val canvasHeight = size.height

        // we first draw the arc which
        // will be the curve of the logo
        drawArc(
            color = Color.White,
            // arc starts at 0 degree and ends
            startAngle = 0f,
            // set use center to false to draw the
            // arc without centered line
            // Tip: use center to true to draw the arc
            // with centered line and see the difference
            useCenter = false,
            // set the end angle of the arc
            sweepAngle = 300f,
            // set the width of the arc and
            // how the arc cap will be drawn
            // cap = StrokeCap.Round will draw
            // the arc with rounded end
            style = Stroke(width = 40f, cap = StrokeCap.Square)
        )

    })

}
