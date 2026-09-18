@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.pantrychef.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.pantrychef.domain.Preferences
import com.mohithash.pantrychef.ui.AppViewModel
import com.mohithash.pantrychef.ui.Label
import com.mohithash.pantrychef.ui.StatCard

/** Shared preference editor (onboarding + settings). */
@Composable
fun PrefsForm(initial: Preferences, onChange: (Preferences) -> Unit) {
    var diet by remember { mutableStateOf(initial.diet) }
    var allergies by remember { mutableStateOf(initial.allergies) }
    var cuisine by remember { mutableStateOf(initial.cuisine) }
    var servings by remember { mutableStateOf(initial.servings.toFloat()) }
    var minutes by remember { mutableStateOf(initial.maxMinutes.toFloat()) }
    fun emit() = onChange(initial.copy(diet = diet, allergies = allergies, cuisine = cuisine, servings = servings.toInt(), maxMinutes = minutes.toInt()))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Label("Diet")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Anything", "Vegetarian", "Vegan", "Halal", "Keto", "Gluten‑free").forEach { d ->
                val v = if (d == "Anything") "" else d
                FilterChip(selected = diet == v, onClick = { diet = v; emit() }, label = { Text(d) })
            }
        }
        OutlinedTextField(allergies, { allergies = it; emit() }, label = { Text("Allergies / avoid") }, placeholder = { Text("peanuts, shellfish…") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
        OutlinedTextField(cuisine, { cuisine = it; emit() }, label = { Text("Favourite cuisines") }, placeholder = { Text("Indian, Italian, Thai…") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
        Label("Servings: ${servings.toInt()}")
        Slider(servings, { servings = it; emit() }, valueRange = 1f..8f, steps = 6)
        Label("Max cooking time: ${minutes.toInt()} min")
        Slider(minutes, { minutes = it; emit() }, valueRange = 10f..120f, steps = 10)
    }
}

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    var draft by remember { mutableStateOf(Preferences()) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(title = { Text("What's in your kitchen?") }, subtitle = { Text("Tell the chef how you eat. Then add your pantry — by photo or by hand.") }, scrollBehavior = scroll) },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard { PrefsForm(Preferences()) { draft = it } }
            Button(onClick = { vm.savePrefs(draft) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Start cooking", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
