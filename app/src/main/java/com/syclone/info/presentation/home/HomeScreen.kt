package com.syclone.info.presentation.home

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

// Home composable function that displays a WebView and a loading indicator
@Composable
fun Home() {
    // Get the current activity context
    val context = LocalContext.current as Activity
    // State to keep track of whether the WebView is loading
    var isLoading by remember { mutableStateOf(true) }

    // Create an AndroidView with a WebView
    AndroidView(factory = { context ->
        WebView(context).apply {
            // Set the WebView visibility based on the loading state
            visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            // Custom WebViewClient to handle page loading events
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    // When page starts loading, set isLoading to true and make WebView invisible
                    isLoading = true
                    view?.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
                }

                override fun onPageFinished(view: WebView, url: String) {
                    // Hide the adhesion header using JavaScript after page load
                    view.evaluateJavascript("javascript:document.querySelector('.adhesion-header').style.display = \"none\"", null)
                    // When page finishes loading, set isLoading to false and make WebView visible
                    isLoading = false
                    view.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
                }
            }
            
            // Enable various settings for the WebView
            settings.apply {
                domStorageEnabled = true
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                defaultTextEncodingName = "utf-8"
            }
            // Load the desired URL
            val url = "https://syclone.info/SWLB-Eeye"
            loadUrl(url)
        }
    }, modifier = Modifier.fillMaxSize())

    // Display a CircularProgressIndicator while the WebView is loading
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = Color(0xFF645FCE),
                strokeWidth = ProgressIndicatorDefaults.StrokeWidth
            )
        }
    }
}
