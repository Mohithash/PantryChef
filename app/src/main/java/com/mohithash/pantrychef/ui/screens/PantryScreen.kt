@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.pantrychef.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mohithash.pantrychef.domain.CATEGORIES
import com.mohithash.pantrychef.ui.AppViewModel
import com.mohithash.pantrychef.ui.EmptyState
import com.mohithash.pantrychef.ui.Job
import com.mohithash.pantrychef.ui.Label
import com.mohithash.pantrychef.ui.MealPhoto
import com.mohithash.pantrychef.ui.Photo
import com.mohithash.pantrychef.ui.ShapeIcon
import com.mohithash.pantrychef.ui.StatCard

@Composable
fun PantryScreen(vm: AppViewModel, onSettings: () -> Unit) {
    val pantry by vm.pantry.collectAsState()
    val scan by vm.scan.collectAsState()
    val ai by vm.ai.collectAsState()
    val cs = MaterialTheme.colorScheme
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Produce") }
    var photo by remember { mutableStateOf<MealPhoto?>(null) }
    val ctx = LocalContext.current
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { b -> b?.let { photo = Photo.fromBitmap(it) } }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { u -> u?.let { photo = Photo.fromUri(ctx, it) } }

    Scaffold(topBar = { TopAppBar(title = { Text("Pantry") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                StatCard(container = cs.secondaryContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ShapeIcon(Icons.Default.PhotoCamera, cs.secondary, cs.onSecondary, MaterialShapes.Cookie7Sided)
                        Column { Text("Scan your fridge", style = MaterialTheme.typography.titleMedium, color = cs.onSecondaryContainer); Label("Photo → catalogued ingredients", cs.onSecondaryContainer.copy(alpha = 0.8f)) }
                    }
                    photo?.let { p ->
                        androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth()) {
                            Image(p.bitmap.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(180.dp).clip(MaterialTheme.shapes.large))
                            IconButton({ photo = null }, Modifier.align(Alignment.TopEnd)) { Icon(Icons.Default.Close, null, tint = cs.onSecondary) }
                        }
                    }
                    when (val s = scan) {
                        Job.Loading -> Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { LoadingIndicator(); Spacer(Modifier.size(10.dp)); Text("Looking…") }
                        is Job.Failed -> { Text(s.message, color = cs.error); OutlinedButton({ vm.clearScan() }) { Text("Dismiss") } }
                        is Job.Done -> {
                            Label("Found ${s.value.size} items", cs.onSecondaryContainer)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { s.value.forEach { InputChip(selected = true, onClick = {}, label = { Text("${it.name}${if (it.qty.isNotBlank()) " · ${it.qty}" else ""}") }) } }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton({ vm.clearScan() }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Text("Discard") }
                                Button({ vm.acceptScan(s.value); photo = null }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Text("Add all") }
                            }
                        }
                        Job.Idle -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton({ camera.launch(null) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Camera") }
                            OutlinedButton({ gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Gallery") }
                            Button({ vm.scanPantry(photo?.base64, "") }, enabled = photo != null && ai.configured, shapes = ButtonDefaults.shapes()) { Text("Scan") }
                        }
                    }
                }
            }
            item {
                StatCard {
                    Label("Add by hand")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(name, { name = it }, label = { Text("Ingredient") }, singleLine = true, modifier = Modifier.weight(2f), shape = MaterialTheme.shapes.large)
                        OutlinedTextField(qty, { qty = it }, label = { Text("Qty") }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { CATEGORIES.forEach { c -> FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) }) } }
                    Button({ vm.addPantry(name, qty, cat); name = ""; qty = "" }, enabled = name.isNotBlank(), shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Spacer(Modifier.size(6.dp)); Text("Add") }
                }
            }
            if (pantry.isEmpty()) item { EmptyState(Icons.Default.Kitchen, "Nothing here yet", "Scan a photo or add ingredients by hand.") }
            pantry.groupBy { it.category }.forEach { (c, items) ->
                item(key = "h$c") { Text(c, style = MaterialTheme.typography.titleMedium, color = cs.primary, modifier = Modifier.padding(top = 8.dp)) }
                item(key = "f$c") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items.forEach { it -> InputChip(selected = false, onClick = { vm.removePantry(it.id) }, label = { Text(if (it.qty.isBlank()) it.name else "${it.name} · ${it.qty}") },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", Modifier.size(16.dp)) }) }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
