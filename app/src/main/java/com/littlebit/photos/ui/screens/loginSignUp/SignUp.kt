package com.littlebit.photos.ui.screens.loginSignUp

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.littlebit.photos.ui.navigation.Screens

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun SignUpScreen(navHostController: NavHostController) {
    val statusBarHeightDp =
            with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }
    val activity = androidx.activity.compose.LocalActivity.current as ComponentActivity
    var initialOrientation by remember { mutableIntStateOf(activity.requestedOrientation) }
    LaunchedEffect(Unit) {
        initialOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    DisposableEffect(Unit) { onDispose { activity.requestedOrientation = initialOrientation } }

    Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .fillMaxHeight(0.35f)
                                .clip(RoundedCornerShape(bottomEnd = 100.dp, bottomStart = 0.dp))
                                .background(Color.Blue.copy(0.5f))
                                .padding(top = statusBarHeightDp),
                contentAlignment = Alignment.Center
        ) {
            Text(
                    text = "Sign Up",
                    fontSize = 32.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(150.dp))
        }
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = "",
                onValueChange = {},
                label = { Text(text = "First Name") },
                shape = RoundedCornerShape(10.dp)
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = "",
                onValueChange = {},
                label = { Text(text = "Last Name") },
                shape = RoundedCornerShape(10.dp)
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = "",
                onValueChange = {},
                label = { Text(text = "Email") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Email, contentDescription = "Email Icon")
                },
                shape = RoundedCornerShape(10.dp)
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = "",
                onValueChange = {},
                label = { Text(text = "Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = "Password Icon")
                },
                shape = RoundedCornerShape(10.dp)
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = "",
                onValueChange = {},
                label = { Text(text = "Confirm Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = "Password Icon")
                },
                shape = RoundedCornerShape(10.dp)
        )
        Spacer(Modifier.height(20.dp))
        ElevatedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Blue.copy(0.5f)),
        ) { Text("Sign Up") }
        Spacer(Modifier.height(40.dp))
        Text(
                text = "Already have any account? Sign in",
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                modifier =
                        Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { navHostController.navigate(Screens.LoginScreen.route) }
                        )
        )
    }
}
