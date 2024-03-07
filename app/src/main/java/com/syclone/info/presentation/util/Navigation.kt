/**
 *
 *	MIT License
 *
 *	Copyright (c) 2023 Gautam Hazarika
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a copy
 *	of this software and associated documentation files (the "Software"), to deal
 *	in the Software without restriction, including without limitation the rights
 *	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *	copies of the Software, and to permit persons to whom the Software is
 *	furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in all
 *	copies or substantial portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *	SOFTWARE.
 *
 **/

package com.syclone.info.presentation.util

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.syclone.info.presentation.home.Home
import com.syclone.info.presentation.home.components.CycloneBottomNavigation
import com.google.accompanist.pager.ExperimentalPagerApi


@OptIn(ExperimentalPagerApi::class)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String,
    scrollState: LazyListState
) {

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Screen.Home.route
        ) {
            Home()
        }


        composable(
            route = Screen.Onboarding.route,
        ) {
            Home()
        }
    }

}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SetupNavigation(startDestination: String) {

    val navController = rememberNavController()


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    val scrollState = rememberLazyListState()
    val state by remember { derivedStateOf { scrollState.firstVisibleItemIndex == 0 } }


    Scaffold(
    ) {
        NavigationGraph(
            navController = navController,
            scrollState = scrollState,
            startDestination = startDestination
        )
    }
}


@Composable
fun BottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    CycloneBottomNavigation(
        backgroundColor = Color(0xFFE1E1E1)

    ) {
        BottomNavigationItem(
            icon =
            {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "",
                )
            },

            selectedContentColor = Color.Black,
            unselectedContentColor = Color.White,
            alwaysShowLabel = false,
            selected = currentRoute == Screen.Home.route,

            onClick = {
                navController.navigate(Screen.Home.route) {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    // on the back stack as users select items
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // reselecting the same item
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                }
            }
        )

        Row() {
            Spacer(modifier = Modifier.width(56.dp))
        }




    }

}
