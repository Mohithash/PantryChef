@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.pantrychef.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.pantrychef.domain.Recipe
import com.mohithash.pantrychef.ui.AppViewModel
import com.mohithash.pantrychef.ui.EmptyState
import com.mohithash.pantrychef.ui.HeroCard
import com.mohithash.pantrychef.ui.Job
import com.mohithash.pantrychef.ui.Label
import com.mohithash.pantrychef.ui.ShapeIcon
import com.mohithash.pantrychef.ui.StatCard
import com.mohithash.pantrychef.ui.theme.Brand

@Composable
fun CookScreen(vm: AppViewModel, onOpen: () -> Unit, onSettings: () -> Unit) {
    val pantry by vm.pantry.collectAsState()
    val job by vm.suggest.collectAsState()
    val ai by vm.ai.collectAsState()
    val cs = MaterialTheme.colorScheme
    var mood by remember { mutableStateOf("") }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(title = { Text("What's for dinner?") }, subtitle = { Text("${pantry.size} items in your pantry") },
                actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, "Settings") } }, scrollBehavior = scroll,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface, scrolledContainerColor = cs.surface))
        },
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Cookie9Sided) {
                    val on = cs.onPrimary
                    Label("Ask the chef", on.copy(alpha = 0.8f))
                    Text("Cook from what you have", style = MaterialTheme.typography.headlineMedium, color = on)
                    OutlinedTextField(mood, { mood = it }, placeholder = { Text("Mood? e.g. quick, spicy, comfort food…", color = on.copy(alpha = 0.6f)) },
                        singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = MaterialTheme.shapes.large,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(focusedTextColor = on, unfocusedTextColor = on, focusedBorderColor = on, unfocusedBorderColor = on.copy(alpha = 0.5f), cursorColor = on))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("15‑minute", "High protein", "Comfort", "Light", "Use up leftovers").forEach { m ->
                            SuggestionChip(onClick = { mood = m }, label = { Text(m) },
                                colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(containerColor = on.copy(alpha = 0.14f), labelColor = on),
                                border = null)
                        }
                    }
                    if (!ai.configured) TextButton(onSettings) { Text("Add your API key to start →", color = cs.secondaryContainer) }
                    Button(
                        onClick = { vm.suggestRecipes(mood) }, enabled = ai.configured && job != Job.Loading, shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.buttonColors(containerColor = cs.secondary, contentColor = cs.onSecondary),
                        modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 4.dp),
                    ) { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text("Suggest recipes") }
                }
            }
            when (val j = job) {
                Job.Loading -> item {
                    Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        LoadingIndicator(); Spacer(Modifier.size(12.dp)); Text("Raiding the pantry…", color = cs.onSurfaceVariant)
                    }
                }
                is Job.Failed -> item { StatCard(container = cs.errorContainer) { Text(j.message, color = cs.onErrorContainer) } }
                is Job.Done -> {
                    item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tonight's ideas", style = MaterialTheme.typography.titleLarge); TextButton(vm::clearSuggestions) { Text("Clear") } } }
                    items(j.value) { r -> RecipeCard(r) { vm.open(r); onOpen() } }
                }
                Job.Idle -> if (pantry.isEmpty()) item {
                    EmptyState(Icons.Default.Kitchen, "Your pantry is empty", "Add ingredients in the Pantry tab — snap a photo of your fridge and the chef will catalogue it.")
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun RecipeCard(r: Recipe, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    StatCard(Modifier.clip(MaterialTheme.shapes.extraLarge).clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShapeIcon(Icons.Default.Schedule, cs.secondaryContainer, cs.onSecondaryContainer, MaterialShapes.Sunny, 44)
            Column(Modifier.weight(1f)) {
                Text(r.title, style = MaterialTheme.typography.titleMedium)
                Text(listOf(r.cuisine, "${r.minutes} min", r.difficulty, if (r.calories_per_serving > 0) "${r.calories_per_serving} kcal" else "").filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
        }
        Text(r.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            val m = r.missing.size
            AssistChip(onClick = onClick, leadingIcon = { Icon(Icons.Default.ShoppingBasket, null, Modifier.size(16.dp)) },
                label = { Text(if (m == 0) "Everything on hand" else "$m to buy") },
                colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(containerColor = if (m == 0) cs.primaryContainer else cs.surfaceContainerHigh))
            r.tags.take(2).forEach { SuggestionChip(onClick = onClick, label = { Text(it) }) }
        }
    }
}
