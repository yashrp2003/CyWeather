package com.syclone.info.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.syclone.info.FCMService
import com.syclone.info.R
import com.syclone.info.presentation.common.SplashViewModel
import com.syclone.info.presentation.util.SetupNavigation
import com.syclone.info.ui.theme.CycloneTheme
import com.syclone.info.ui.theme.Purple500
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var permission : AppPermission

    @Inject
    lateinit var splashViewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            splashViewModel.isLoading.value
        }
        setContent {
            CycloneTheme {
                val screen by splashViewModel.startDestination

                Surface(
                    modifier = Modifier.fillMaxSize(),
                )
                {
                    SetupNavigation(
                        startDestination = screen
                    )
                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                            Log.d("FCM Notify", "Fetching FCM registration token failed", task.exception)
                            return@OnCompleteListener
                        }

                        //Get new FCM registration token
                        val token: String? = task.result
                        Log.d("FCM Token", token?:"Token is Null")
                        //Toast.makeText(this, token, Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
        permission = AppPermission()

        if (permission.isLocationOk(this)){
            println("Allowed")
        }else{
            permission.requestLocationPermission(this)
            println("denied")
        }
    }
}

