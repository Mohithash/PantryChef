@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.pantrychef.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mohithash.pantrychef.ui.screens.CookScreen
import com.mohithash.pantrychef.ui.screens.GroceryScreen
import com.mohithash.pantrychef.ui.screens.OnboardingScreen
import com.mohithash.pantrychef.ui.screens.PantryScreen
import com.mohithash.pantrychef.ui.screens.RecipeDetailScreen
import com.mohithash.pantrychef.ui.screens.RecipesScreen
import com.mohithash.pantrychef.ui.screens.SettingsScreen

enum class Tab(val route: String, val label: String, val icon: ImageVector, val selected: ImageVector) {
    COOK("cook", "Cook", Icons.Outlined.RestaurantMenu, Icons.Filled.RestaurantMenu),
    PANTRY("pantry", "Pantry", Icons.Outlined.Kitchen, Icons.Filled.Kitchen),
    RECIPES("recipes", "Recipes", Icons.Outlined.MenuBook, Icons.Filled.MenuBook),
    GROCERY("grocery", "Grocery", Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
}

@Composable
fun Nav(vm: AppViewModel) {
    val prefs by vm.prefs.collectAsState()
    if (!prefs.onboarded) { OnboardingScreen(vm); return }

    val nav = rememberNavController()
    val back by nav.currentBackStackEntryAsState()
    val current = back?.destination
    val showBar = Tab.entries.any { t -> current?.hierarchy?.any { it.route == t.route } == true }

    Scaffold(bottomBar = {
        if (showBar) NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            Tab.entries.forEach { tab ->
                val sel = current?.hierarchy?.any { it.route == tab.route } == true
                NavigationBarItem(selected = sel, onClick = {
                    nav.navigate(tab.route) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true }
                }, icon = { Icon(if (sel) tab.selected else tab.icon, tab.label) }, label = { Text(tab.label) })
            }
        }
    }) { pad ->
        NavHost(nav, Tab.COOK.route, Modifier.padding(bottom = pad.calculateBottomPadding())) {
            composable(Tab.COOK.route) { CookScreen(vm, onOpen = { nav.navigate("detail") }, onSettings = { nav.navigate("settings") }) }
            composable(Tab.PANTRY.route) { PantryScreen(vm, onSettings = { nav.navigate("settings") }) }
            composable(Tab.RECIPES.route) { RecipesScreen(vm, onOpen = { nav.navigate("detail") }) }
            composable(Tab.GROCERY.route) { GroceryScreen(vm) }
            composable("detail") { RecipeDetailScreen(vm, onBack = { nav.popBackStack() }) }
            composable("settings") { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
        }
    }
}
