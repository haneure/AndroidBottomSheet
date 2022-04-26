package com.haneure.bottomsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class Header {
    data class HeaderPlain(val titleText: String): Header()
    data class HeaderImage(val titleText: String, val imageResourceId: Int?): Header()
}

sealed class Content {
    data class Center(val valueText: String): Content()
    data class Left(val valueText: String): Content()
}

sealed class Footer {
    object Plain: Footer()
    sealed class ButtonSingle: Footer(){
        data class NegativeButton(val negativeBtnLabel: String, val onClickNegative: (() -> Unit)?): Footer()
        data class PositiveButton(val positiveBtnLabel: String, val onClickPositive: (() -> Unit)?): Footer()
    }
    data class ButtonMultiple(
        val negativeBtnLabel: String,
        val onClickNegative: (() -> Unit)?,
        val positiveBtnLabel: String,
        val onClickPositive: (() -> Unit)?
    ): Footer()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //Header with image
//            ModalBottomSheet(
//                header = Header.HeaderPlain("Title here"),
//                content = Content.Center("Content here"),
//                footer = Footer.ButtonMultiple("No", {} , "Yes", {})
//            )

            //Header without image and single positive button
            ModalBottomSheet(
                header = Header.HeaderPlain("Title here"),
                content = Content.Center("Content here"),
                footer = Footer.ButtonSingle.PositiveButton("Yes", {})
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModalBottomSheet(
    header: Header,
    content: Content,
    footer: Footer
) {
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded)
    val coroutine = rememberCoroutineScope()
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.padding(0.dp)
        ,
        sheetContent = {
            BottomSheetContent(BottomSheetState(header, content, footer), coroutine, sheetState)
        }
    ) {
        MainScreen(coroutine, sheetState)
    }
}

@OptIn(ExperimentalMaterialApi::class)
class BottomSheetState(
    header: Header,
    content: Content,
    footer: Footer
) {
    var imageResourceId: Int? = null
        private set
    var titleText = ""
        private set
    var valueText = ""
        private set
    var negativeLabel = ""
        private set
    var positiveLabel = ""
        private set
    var negativeButton = false
        private set
    var positiveButton = false
        private set
    var onClickNegative: (() -> Unit)? = null
        private set
    var onClickPositive: (() -> Unit)? = null
        private set
    var alignValue = Alignment.CenterHorizontally
        private set

    init {
        when(header) {
            is Header.HeaderPlain -> {
                titleText = header.titleText
            }
            is Header.HeaderImage -> {
                titleText = header.titleText
                imageResourceId = header.imageResourceId
            }
        }

        when(content) {
            is Content.Center -> {
                valueText = content.valueText
                alignValue = Alignment.CenterHorizontally
            }
            is Content.Left -> {
                valueText = content.valueText
                alignValue = Alignment.Start
            }
        }

        when(footer) {
            is Footer.ButtonSingle.NegativeButton -> {
                negativeButton = true
                negativeLabel = footer.negativeBtnLabel
                onClickNegative = footer.onClickNegative
            }
            is Footer.ButtonSingle.PositiveButton -> {
                positiveButton = true
                positiveLabel = footer.positiveBtnLabel
                onClickPositive = footer.onClickPositive
            }
            is Footer.ButtonMultiple-> {
                negativeButton = true
                positiveButton = true
                negativeLabel = footer.negativeBtnLabel
                positiveLabel = footer.positiveBtnLabel
                onClickNegative = footer.onClickNegative
                onClickPositive = footer.onClickPositive
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetContent(
    state: BottomSheetState,
    coroutineScope: CoroutineScope,
    sheetState: ModalBottomSheetState
) {
    val configuration = LocalConfiguration.current

    Box(modifier = Modifier.fillMaxWidth()
        .heightIn(
            (configuration.screenHeightDp * 0.25).dp,
            (configuration.screenHeightDp * 0.75).dp
        )
        .wrapContentWidth(unbounded = false)
        .wrapContentHeight(unbounded = true)
        .padding(24.dp, 24.dp, 24.dp, 32.dp)
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = state.alignValue
        ) {
            state.imageResourceId?.let {
                header(state.imageResourceId!!)
            }
            Spacer(modifier = Modifier.height(24.dp))

            content(state.titleText, state.valueText)

            Spacer(modifier = Modifier.height(24.dp))

            footer(
                state.negativeLabel,
                state.negativeButton,
                state.positiveLabel,
                state.positiveButton,
                state.onClickNegative,
                state.onClickPositive,
                coroutineScope,
                sheetState
            )
        }
    }
}

@Composable
fun header(
    imageResourceId: Int
) {
    Box(
        modifier = Modifier
    ) {
        Image(painter = painterResource(id = imageResourceId), contentDescription = "Image")
    }
}

@Composable
fun content(
    titleText: String,
    valueText: String
){
    Text(text = titleText)
    Spacer(modifier = Modifier.height(24.dp))
    Text(text = valueText)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun footer(
    negativeLabel: String,
    negativeButton: Boolean,
    positiveLabel: String,
    positiveButton: Boolean,
    onClickNegative: (() -> Unit)?,
    onClickPositive: (() -> Unit)?,
    coroutine: CoroutineScope,
    sheetState: ModalBottomSheetState
) {
    if(positiveButton){
        Button(onClick = onClickPositive!!) {
            Text(text = positiveLabel)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    if(negativeButton){
        OutlinedButton(
            onClick = {
                bottomSheetVisibility(coroutine, sheetState)
            }
        ) {
            Text(text = negativeLabel)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(scope: CoroutineScope, state: ModalBottomSheetState) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.white),
                contentColor = colorResource(id = R.color.black)
            ),
            onClick = {
                scope.launch {
                    state.show()
                }
            }) {
            Text(text = "Open Modal Bottom Sheet Layout")
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun bottomSheetVisibility(coroutineScope: CoroutineScope, sheetState: ModalBottomSheetState){
    coroutineScope.launch {
        if (sheetState.currentValue == ModalBottomSheetValue.Hidden) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }
}