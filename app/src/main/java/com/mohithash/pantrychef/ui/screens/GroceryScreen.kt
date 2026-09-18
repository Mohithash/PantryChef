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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mohithash.pantrychef.ui.AppViewModel
import com.mohithash.pantrychef.ui.EmptyState

@Composable
fun GroceryScreen(vm: AppViewModel) {
    val list by vm.grocery.collectAsState()
    val cs = MaterialTheme.colorScheme
    var text by remember { mutableStateOf("") }
    val done = list.count { it.done }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Grocery list") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface),
            actions = { if (done > 0) TextButton(vm::clearDone) { Text("Clear $done done") } })
    }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(text, { text = it }, placeholder = { Text("Add an item") }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
                    Button({ vm.addGrocery(text); text = "" }, enabled = text.isNotBlank(), shapes = ButtonDefaults.shapes()) { Text("Add") }
                }
            }
            if (list.isEmpty()) item { EmptyState(Icons.Default.ShoppingCart, "List is empty", "Missing ingredients from recipes land here.") }
            items(list, key = { it.id }) { g ->
                ListItem(
                    leadingContent = { Checkbox(g.done, { vm.toggleGrocery(g) }) },
                    headlineContent = { Text(g.name, textDecoration = if (g.done) TextDecoration.LineThrough else null, color = if (g.done) cs.onSurfaceVariant else cs.onSurface) },
                    supportingContent = { if (g.forRecipe.isNotBlank()) Text("for ${g.forRecipe}") },
                    trailingContent = { IconButton({ vm.deleteGrocery(g.id) }) { Icon(Icons.Default.Delete, null, tint = cs.onSurfaceVariant) } },
                    colors = ListItemDefaults.colors(containerColor = if (g.done) cs.surfaceContainerLowest else cs.surfaceContainerLow),
                    modifier = Modifier.clip(MaterialTheme.shapes.large),
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
