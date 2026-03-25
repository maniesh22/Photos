package com.littlebit.photos.ui.screens.loginSignUp

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.littlebit.photos.MainActivity
import com.littlebit.photos.R
import com.littlebit.photos.ui.navigation.Screens

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun LoginScreen(navHostController: NavHostController) {
    val mainActivity = androidx.activity.compose.LocalActivity.current as MainActivity
    val statusBarHeight = WindowInsets.statusBars.getTop(LocalDensity.current)
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
                                .background(Color.Blue.copy(0.5f)),
                contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                    model = R.drawable.login_vector,
                    contentDescription = null,
                    modifier = Modifier.padding(top = statusBarHeight.dp).fillMaxSize(0.5f)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
                text = "Welcome back!",
                fontSize = 24.sp,
                letterSpacing = 4.sp,
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontStyle = MaterialTheme.typography.titleMedium.fontStyle
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
        ElevatedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Blue.copy(0.5f)),
        ) { Text("Login") }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.width(120.dp))
            Text(
                    "Or",
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
            HorizontalDivider(Modifier.width(120.dp))
        }
        Spacer(Modifier.height(20.dp))
        Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
        ) {
            /** ************* To be Added */
            /*
            IconButton({}) {
                AsyncImage(
                    model = R.drawable.icons_facebook,
                    contentDescription = "Facebook Icon",
                    modifier = Modifier.size(40.dp)
                )
            }
            */
            IconButton({ mainActivity.signInWithGoogle() }) {
                AsyncImage(
                        model = R.drawable.icon_google,
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(
                text = "Don't have any account? Signup",
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                modifier =
                        Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { navHostController.navigate(Screens.SignUpScreen.route) }
                        )
        )
        Spacer(Modifier.height(20.dp))
    }
}
