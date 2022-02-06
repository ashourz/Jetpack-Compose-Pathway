package com.development.ashou.mycomposeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.development.ashou.mycomposeapplication.ui.theme.ui.theme.JetpackComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
           JetpackComposeTheme {
                UsingStateInJetpackCompose()
            }
        }
    }
}




@Composable
fun UsingStateInJetpackCompose() {

}


