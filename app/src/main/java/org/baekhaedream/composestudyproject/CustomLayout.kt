package org.baekhaedream.composestudyproject

import android.graphics.Color
import android.view.RoundedCorner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import org.baekhaedream.composestudyproject.ui.theme.ComposeStudyProjectTheme
import kotlin.math.max

fun Modifier.firstBaselineToTop(
    firstBaselineToTop: Dp
) = this.then(
    //mesurable : 측정되고 위치되어질 자식
    //constraints : 자식의 최소, 최대 넓이, 높이
    layout { measurable, constraints ->
        //위치, 크기 정보를 담고 있는 객체
        val placeable = measurable.measure(constraints = constraints)
        check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
        val firstBaseline = placeable[FirstBaseline] //첫 번째 베이스 라인의 위치

        val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
        val height = placeable.height + placeableY
        layout(placeable.width, height) {
            placeable.placeRelative(0, placeableY)
        }
    }
)


//Layout ->  layout 의 여러 자식들을 어떻게 배치할지 결정할 수 있다
//일단 먼저 Column 을 구현해보자

@Composable
fun MyOwnColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        var width = 0
        //자식을 측정하는 부분 -> 자식들의 크기를 측정하고, 자식의 최대 넓이를 저장한다.
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints = constraints)
            width = max(width, placeable.width)
            placeable
        }

        //이 레이아웃의 크기는, 자식의 최대 넓이로 지정한다.
        var y = 0
        layout(width = width, constraints.maxHeight) {
            for (placeable in placeables) {

                placeable.placeRelative(0, y)
                y += placeable.height
            }
        }
    }
}

@Composable
fun MyOwnRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    Layout(modifier = modifier, content = content) { m, c ->
        val placeables = m.map { measurable ->
            measurable.measure(c)
        }

        var x = 0
        layout(c.maxWidth, c.maxHeight) {
            for (placeable in placeables) {
                placeable.placeRelative(x, 0)
                x += placeable.width
            }
        }
    }
}

@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    rows: Int = 3,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { m, c ->

        val rowWidths = IntArray(rows) { 0 }
        val rowHeights = IntArray(rows) { 0 }

        val placeables = m.mapIndexed { index, measurable ->
            val placeable = measurable.measure(c)
            val row = index % rows
            rowWidths[row] += placeable.width
            rowHeights[row] = Math.max(rowHeights[row], placeable.height)
            placeable
        }
        val width = rowWidths.maxOrNull()?.coerceIn(c.minWidth.rangeTo(c.maxWidth)) ?: c.minWidth
        val height = rowHeights.sumOf { it }.coerceIn(c.minHeight.rangeTo(c.maxHeight))
        val rowY = IntArray(rows) { 0 }
        for (i in 1 until rows) {
            rowY[i] = rowY[i - 1] + rowHeights[i - 1]
        }
        layout(width = width, height = height) {
            val rowX = IntArray(rows) { 0 }

            placeables.forEachIndexed { index, placeable ->
                val row = index % rows
                placeable.placeRelative(x = rowX[row], y = rowY[row])
                rowX[row] += placeable.width
            }
        }
    }
}

@Composable
fun Chip(modifier: Modifier = Modifier, text: String) {
    Card(
        modifier = modifier,
        border = BorderStroke(color = Black, width = Dp.Hairline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp, 16.dp)
                    .background(color = MaterialTheme.colors.secondary)
            )
            Spacer(Modifier.width(4.dp))
            Text(text = text)
        }
    }
}

@Preview
@Composable
fun ChipPreview() {
    ComposeStudyProjectTheme() {
        Chip(text = "Hi there")
    }
}

val topics = listOf(
    "Arts & Crafts", "Beauty", "Books", "Business", "Comics", "Culinary",
    "Design", "Fashion", "Film", "History", "Maths", "Music", "People", "Philosophy",
    "Religion", "Social sciences", "Technology", "TV", "Writing"
)

@Composable
fun BodyContent(modifier: Modifier = Modifier) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        StaggeredGrid(rows = 3) {
            for (topic in topics) {
                Chip(modifier = Modifier.padding(8.dp), text = topic)
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun BodyContentPreview() {
    ComposeStudyProjectTheme {
        StaggeredGrid(rows = 3) {
            for (topic in topics) {
                Chip(modifier = Modifier.padding(8.dp), text = topic)
            }
        }
    }
}