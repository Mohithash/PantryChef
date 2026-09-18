@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.pantrychef.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.pantrychef.ui.AppViewModel
import com.mohithash.pantrychef.ui.Label
import com.mohithash.pantrychef.ui.ShapeIcon
import com.mohithash.pantrychef.ui.StatCard
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val ai by vm.ai.collectAsState()
    val prefs by vm.prefs.collectAsState()
    var draft by remember { mutableStateOf(prefs) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) },
        snackbarHost = { SnackbarHost(snack) },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AiSettingsCard(ai, vm.client, vm::saveAi) { m -> scope.launch { snack.showSnackbar(m) } }
            StatCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShapeIcon(Icons.Default.Tune, cs.secondaryContainer, cs.onSecondaryContainer, MaterialShapes.Sunny)
                    Column { Text("How you eat", style = MaterialTheme.typography.titleMedium); Label("Diet, allergies, servings") }
                }
                PrefsForm(prefs) { draft = it }
                Button({ vm.savePrefs(draft); scope.launch { snack.showSnackbar("Preferences saved") } }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Save preferences") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
