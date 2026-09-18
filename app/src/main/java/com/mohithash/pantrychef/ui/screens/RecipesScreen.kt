@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.pantrychef.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.pantrychef.ui.AppViewModel
import com.mohithash.pantrychef.ui.EmptyState

@Composable
fun RecipesScreen(vm: AppViewModel, onOpen: () -> Unit) {
    val recipes by vm.recipes.collectAsState()
    val cs = MaterialTheme.colorScheme
    Scaffold(topBar = { TopAppBar(title = { Text("My recipes") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface)) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (recipes.isEmpty()) item { EmptyState(Icons.Default.MenuBook, "No saved recipes", "Tap the heart on any suggestion to keep it here.") }
            items(recipes, key = { it.id }) { row ->
                val r = vm.decode(row)
                androidx.compose.foundation.layout.Column {
                    RecipeCard(r) { vm.open(r, row); onOpen() }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(listOf(if (row.favorite) "♥ favourite" else "", if (row.cookedCount > 0) "cooked ${row.cookedCount}×" else "").filter { it.isNotBlank() }.joinToString("  ·  "),
                            style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                        TextButton({ vm.deleteRecipe(row.id) }) { Icon(Icons.Default.Delete, null, Modifier.height(16.dp)); Spacer(Modifier.height(0.dp)); Text(" Remove") }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
