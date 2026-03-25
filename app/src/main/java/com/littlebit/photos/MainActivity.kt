@file:Suppress("DEPRECATION")

package com.littlebit.photos

import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.littlebit.photos.ui.navigation.NavigationGraph
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.audio.player.PlayAudioViewModel
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.settings.SettingsViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import com.littlebit.photos.ui.theme.PhotosTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@Suppress("DEPRECATION")

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100
    private val photosViewModel: PhotosViewModel by viewModels()
    private val videoViewModel: VideoViewModel by viewModels()
    private val audioViewModel: AudioViewModel by viewModels()
    private val playAudioViewModel: PlayAudioViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        settingsViewModel.updateTheme()
        photosViewModel.getData(this)
        videoViewModel.getData(this)
        audioViewModel.getData(this)
        setContent {
            BitMediaApp(photosViewModel, videoViewModel, audioViewModel, playAudioViewModel, settingsViewModel)
        }
    }



    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, you can now call deletePhoto() again.
                } else {
                    // Permission denied. You can inform the user or handle it accordingly.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?, caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign-in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("GoogleSignIn", "Sign-in successful: ${user?.displayName}")
                } else {
                    Log.w("GoogleSignIn", "Sign-in failed", task.exception)
                }
            }
    }


    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


}


@Composable
fun BitMediaApp(
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel,
    audioViewModel: AudioViewModel,
    playAudioViewModel: PlayAudioViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val currentTheme by settingsViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val systemTheme = isSystemInDarkTheme()
    val navController = rememberNavController()

    val startDestination = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination.value = getStartDestination(context)
    }

    if (startDestination.value != null) {
        PhotosTheme(
            darkTheme = currentTheme ?: systemTheme,
            dynamicColor = true
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavigationGraph(
                    navController = navController,
                    startDestination = startDestination.value!!,
                    photosViewModel = photosViewModel,
                    videoViewModel = videoViewModel,
                    audioViewModel = audioViewModel,
                    playAudioViewModel = playAudioViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    } else {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    }
}
