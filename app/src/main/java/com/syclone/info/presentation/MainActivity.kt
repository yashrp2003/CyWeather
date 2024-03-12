package com.syclone.info.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.messaging.FirebaseMessaging
import com.syclone.info.FCMService
import com.syclone.info.R
import com.syclone.info.presentation.common.SplashViewModel
import com.syclone.info.presentation.util.SetupNavigation
import com.syclone.info.ui.theme.CycloneTheme
import com.syclone.info.ui.theme.Purple500
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import javax.inject.Inject
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices



@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var splashViewModel: SplashViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient


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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            requestLocation()
        }
    }
    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Log.d(TAG, "Latitude: $latitude, Longitude: $longitude")
                    // You can use latitude and longitude here
                } else {
                    Log.e(TAG, "Unable to fetch location")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting location: ${e.message}")
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 100
        private const val TAG = "MainActivity"
    }
}

