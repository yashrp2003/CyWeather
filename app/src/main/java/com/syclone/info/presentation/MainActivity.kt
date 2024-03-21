package com.syclone.info.presentation

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import android.content.res.Configuration
import com.google.android.gms.location.Priority


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var splashViewModel: SplashViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private var isPermissionRequested = false


    companion object {
        private const val REQUEST_CODE_LOCATION_PERMISSION = 100
    }

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
        requestPermission()
    }
    private fun getDeviceLocale(context: Context): java.util.Locale? {
        return context.resources.configuration.locales[0]
    }

    private fun requestPermission() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    getLocationAndOpenUrl()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    getLocationAndOpenUrl()
                }
                else -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Location Access Required")
                    builder.setMessage("This app requires access to your location to provide accurate weather information. Please grant location permission.")
                    builder.setPositiveButton("Grant Permission") { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ),
                            REQUEST_CODE_LOCATION_PERMISSION
                        )
                    }
                    builder.setNegativeButton("Exit App") { dialog, which ->
                        finish()
                    }
                    builder.setCancelable(false)
                    builder.show()
                }
            }
        }
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting location and opening URL
                getLocationAndOpenUrl()
            } else {
                // Permission denied, handle it accordingly
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getLocationAndOpenUrl() {
        if (!isPermissionRequested &&
            (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED))
        {
            return
        }
        else {
            val locationRequest = LocationRequest.Builder(1000L)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    fusedLocationClient.removeLocationUpdates(this)

                    val location = locationResult.lastLocation

                    if (location == null) {
                        Toast.makeText(this@MainActivity, "Cannot get location.", Toast.LENGTH_SHORT).show()
                    } else {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        Log.d("Loc", "Latitude: $latitude, Longitude: $longitude")
                        val locale = getDeviceLocale(this@MainActivity)
                        val lan = locale?.language
                        val url =
                            "https://www.accuweather.com/ajax-service/selectLatLon?lat=$latitude&lon=$longitude&unit=C&lang=$lan&partner=web_syclonetec_logicom_adc"

                        Log.d("LocationActivity", "URL: $url")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        finish()
                    }
                }
            }, null)
        }
    }
}

