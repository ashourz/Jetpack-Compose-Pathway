package com.development.ashou.mycomposeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import coil.compose.rememberImagePainter
import com.development.ashou.mycomposeapplication.ui.theme.LayoutsInJetpackComposeTheme
import kotlinx.coroutines.launch
import java.util.function.DoublePredicate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LayoutsInJetpackComposeTheme {
                LayoutsCodelab()
            }
        }
    }
}



/**
 * Scaffold Example
 * */
@Composable
fun LayoutsCodelab() {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = "LayoutsCodelab")
            },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Favorite, contentDescription = null)
                    }
                })
        }
    ) { innerPadding ->
        BodyContent(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp)
        )
    }
}



/**
 * Extension to add padding to element from top of frame to the baseline of the text
 * */
fun Modifier.firstBaselineToTop(firstBaselineToTop: Dp) = this.then(
    layout{ measurable, constraints ->
        val placeable = measurable.measure(constraints)

        //Check the composable has a first baseline
        check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
        val firstBaseline = placeable[FirstBaseline]

        //Height of the composable with padding - first baseline
        val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
        val height = placeable.height + placeableY
        layout(
            width = placeable.width,
            height = height){
            // If you don't call placeRelative, the composable won't be visible.
            // placeRelative automatically adjusts the position of the placeable
            // based on the current layoutDirection.
            placeable.placeRelative(x = 0, y = placeableY)
        }
    }
)

/**
 * Custom Layouts
 * */
//Custom Column Implementation
@Composable
fun MyOwnColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        // Measure and position children given constraints logic here
        val placeables = measurables.map{ measurable ->
            //Measure each child
            // you can only measure children once
            measurable.measure(constraints)
        }
        // Track the y co-ord we have placed children up to
        var yPosition = 0
        // Set the size of the layout as big as it can
        layout(constraints.maxWidth, constraints.maxHeight){
            // Place children
            placeables.forEach{ placeable ->
                // Position item on the screen
                placeable.placeRelative( x = 0, y = yPosition)

                //Record the y co-ord place up to
                yPosition += placeable.height
            }
        }
    }
}

// Custom Staggered Grid Layout
@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    rows: Int = 3,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        /** measure and position children given constraints logic here
         *  you can only measure children once
        */
        //Keep track of the width of each row
        val rowWidths = IntArray(rows){ 0 }

        //Keep track of the max height of each row
        val rowHeights = IntArray(rows){ 0 }

        /*** Don't constrain child views further, measure them with given constraints */
        // List of measured children
        val placeables = measurables.mapIndexed { index, measurable ->

            // Measure each child
            val placeable = measurable.measure(constraints)

            // Track the width and max height of each row
            val row = index % rows
            rowWidths[row] += placeable.width
            rowHeights[row] = Math.max(rowHeights[row], placeable.height)

            placeable
        }

        // Grid's width is the widest row
        val width = rowWidths.maxOrNull()
            ?.coerceIn(constraints.minWidth.rangeTo(constraints.maxWidth)) ?: constraints.minWidth

        // Grid's height is the sum of the tallest element of each row
        // coerced to the height constraints
        val height = rowHeights.sumOf { it }
            .coerceIn(constraints.minHeight.rangeTo(constraints.maxHeight))

        // Y of each row, based on the height accumulation of previous rows
        val rowY = IntArray(rows) { 0 }
        for (i in 1 until rows) {
            rowY[i] = rowY[i-1] + rowHeights[i-1]
        }

        // Set the size of the parent layout
        layout(width, height) {
            // x cord we have placed up to, per row
            val rowX = IntArray(rows) { 0 }

            placeables.forEachIndexed { index, placeable ->
                val row = index % rows
                placeable.placeRelative(
                    x = rowX[row],
                    y = rowY[row]
                )
                rowX[row] += placeable.width
            }
        }
    }
}

/** Constraint Layout */
@Composable
fun ConstraintLayoutContent() {
    ConstraintLayout {

        // Create references for the composables to constrain
        val (button, text) = createRefs()

        Button(
            onClick = { /* Do something */ },
            // Assign reference "button" to the Button composable
            // and constrain it to the top of the ConstraintLayout
            modifier = Modifier.constrainAs(button) {
                top.linkTo(parent.top, margin = 16.dp)
            }
        ) {
            Text("Button")
        }

        // Assign reference "text" to the Text composable
        // and constrain it to the bottom of the Button composable
        Text("Text", Modifier.constrainAs(text) {
            top.linkTo(button.bottom, margin = 16.dp)
            // Centers Text horizontally in the ConstraintLayout
            centerHorizontallyTo(parent)
        })
    }
}

@Composable
fun ConstraintLayoutContent2() {
    ConstraintLayout {
        // Creates references for the three composables
        // in the ConstraintLayout's body
        val (button1, button2, text) = createRefs()

        Button(
            onClick = { /* Do something */ },
            modifier = Modifier.constrainAs(button1) {
                top.linkTo(parent.top, margin = 16.dp)
            }
        ) {
            Text("Button 1")
        }

        Text("Text", Modifier.constrainAs(text) {
            top.linkTo(button1.bottom, margin = 16.dp)
            centerAround(button1.end)
        })

        // barriers (and all the other helpers) can be created in the body of ConstraintLayout,
        // but not inside constrainAs.
        val barrier = createEndBarrier(button1, text)
        Button(
            onClick = { /* Do something */ },
            modifier = Modifier.constrainAs(button2) {
                top.linkTo(parent.top, margin = 16.dp)
                // linkTo can be used to constrain with guidelines and barriers the same way it
                // works for edges of layouts.
                start.linkTo(barrier)
            }
        ) {
            Text("Button 2")
        }
    }
}

@Composable
fun DecoupledConstraintLayout() {
    BoxWithConstraints {
        val constraints = if (maxWidth < maxHeight) {
            decoupledConstraints(margin = 16.dp) // Portrait constraints
        } else {
            decoupledConstraints(margin = 32.dp) // Landscape constraints
        }

        ConstraintLayout(constraints) {
            Button(
                onClick = { /* Do something */ },
                modifier = Modifier.layoutId("button")
            ) {
                Text("Button")
            }

            Text("Text", Modifier.layoutId("text"))
        }
    }
}

private fun decoupledConstraints(margin: Dp): ConstraintSet {
    return ConstraintSet {
        val button = createRefFor("button")
        val text = createRefFor("text")

        constrain(button) {
            top.linkTo(parent.top, margin= margin)
        }
        constrain(text) {
            top.linkTo(button.bottom, margin)
        }
    }
}

@Preview
@Composable
fun ConstraintLayoutContentPreview() {
    LayoutsInJetpackComposeTheme {
        ConstraintLayoutContent()
    }
}

@Preview
@Composable
fun ConstraintLayoutContent2Preview() {
    LayoutsInJetpackComposeTheme {
        ConstraintLayoutContent2()
    }
}

@Preview
@Composable
fun ConstraintLayoutContent3Preview() {
    LayoutsInJetpackComposeTheme {
        DecoupledConstraintLayout()
    }
}

/**
   Intrinsics
   To a composable, you can ask for its intrinsicWidth or intrinsicHeight:
    (min|max)IntrinsicWidth:
        Given this height, what's the minimum/maximum width you can paint your content properly.

    (min|max)IntrinsicHeight:
        Given this width, what's the minimum/maximum height you can paint your content properly.
 * */
@Composable
fun TwoTexts(modifier: Modifier = Modifier, text1: String, text2: String) {
    //height(IntrinsicSize.Min) sizes its children being forced to be as tall as their minimum
    // intrinsic height. As it's recursive, it'll query Row and its children minIntrinsicHeight.
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
                .wrapContentWidth(Alignment.Start),
            text = text1
        )

        Divider(color = Color.Black, modifier = Modifier.fillMaxHeight().width(1.dp))
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .wrapContentWidth(Alignment.End),

            text = text2
        )
    }
}

@Preview
@Composable
fun TwoTextsPreview() {
    LayoutsInJetpackComposeTheme {
        Surface {
            TwoTexts(text1 = "Hi", text2 = "there")
        }
    }
}

/**
 * Additional Composables
 * */

@Composable
fun Chip(modifier: Modifier = Modifier, text: String) {
    Card(
        modifier = modifier,
        border = BorderStroke(color = Color.Black, width = Dp.Hairline),
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

@Composable
fun BodyContent(modifier: Modifier = Modifier) {
    MyOwnColumn(modifier.padding(8.dp)) {
        Text("MyOwnColumn")
        Text("places items")
        Text("vertically.")
        Text("We've done it by hand!")
    }
}
val topics = listOf(
    "Arts & Crafts", "Beauty", "Books", "Business", "Comics", "Culinary",
    "Design", "Fashion", "Film", "History", "Maths", "Music", "People", "Philosophy",
    "Religion", "Social sciences", "Technology", "TV", "Writing"
)


@Composable
fun StaggeredBodyContent(modifier: Modifier = Modifier) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        StaggeredGrid(modifier = modifier) {
            for (topic in topics) {
                Chip(modifier = Modifier.padding(8.dp), text = topic)
            }
        }
    }
}

@Composable
fun SimpleList() {
    val listSize = 100
    //We save the scrolling position with this state
    val scrollState = rememberLazyListState()
    //We save the coroutine scope when our animated scroll will be executed
    val coroutineScope = rememberCoroutineScope()
    Column {
        Row {
            Button(onClick = {
                coroutineScope.launch {
                    //0 is the first item index
                    scrollState.animateScrollToItem(0)
                }
            }) {
                Text("Scroll to the top")
            }
            Button(onClick = {
                coroutineScope.launch {
                    //listSize-1 is the last item index
                    scrollState.animateScrollToItem(listSize - 1)
                }
            }) {
                Text("Scroll to the end")
            }
        }
        LazyColumn(state = scrollState) {
            items(listSize) {
                ImageListItem(it)
            }
        }
    }
}

@Composable
fun ImageListItem(index: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = rememberImagePainter(
                data = "https://developer.android.com/images/brand/Android_Robot.png"
            ),
            contentDescription = "Android Logo",
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("Item #$index", style = MaterialTheme.typography.subtitle1)


    }
}

@Composable
fun PhotographerCard() {
    Row {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
        ) {
            //Image goes here
        }
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = "Alfred Sisley",
                fontWeight = FontWeight.Bold
            )
            //LocalContentAlpha is defining opacity level of its children
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text("3 minutes ago", style = MaterialTheme.typography.body2)
            }
        }
    }
}


/**
 * Previews
 * */
@Preview
@Composable
fun LayoutsCodelabPreview() {
    LayoutsInJetpackComposeTheme {
        StaggeredBodyContent()
    }
}

@Preview
@Composable
fun ChipPreview() {
    LayoutsInJetpackComposeTheme {
        Chip(text = "Hi there")
    }
}

@Preview
@Composable
fun TextWithPaddingToBaselinePreview() {
    LayoutsInJetpackComposeTheme {
        Text("Hi there!", Modifier.firstBaselineToTop(32.dp))
    }
}

@Preview
@Composable
fun TextWithNormalPaddingPreview() {
    LayoutsInJetpackComposeTheme {
        Text("Hi there!", Modifier.padding(top = 32.dp))
    }
}

@Preview
@Composable
fun SimpleListPreview() {
    LayoutsInJetpackComposeTheme {
        SimpleList()
    }
}

@Preview
@Composable
fun LayoutCodelabPreview() {
    LayoutsInJetpackComposeTheme {
        LayoutsCodelab()
    }
}

@Preview
@Composable
fun PhotographerCardPreview() {
    LayoutsInJetpackComposeTheme {
        PhotographerCard()
    }
}