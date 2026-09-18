@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.pantrychef.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.pantrychef.ui.AppViewModel
import com.mohithash.pantrychef.ui.HeroCard
import com.mohithash.pantrychef.ui.Job
import com.mohithash.pantrychef.ui.Label
import com.mohithash.pantrychef.ui.ShapeIcon
import com.mohithash.pantrychef.ui.StatCard
import com.mohithash.pantrychef.ui.theme.Brand
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailScreen(vm: AppViewModel, onBack: () -> Unit) {
    val sel by vm.selected.collectAsState()
    val (r, row) = sel ?: run { onBack(); return }
    val recipes by vm.recipes.collectAsState()
    val saved = row?.let { rr -> recipes.firstOrNull { it.id == rr.id } } ?: recipes.firstOrNull { it.title == r.title }
    val sub by vm.substitution.collectAsState()
    val cs = MaterialTheme.colorScheme
    var cooking by remember { mutableStateOf(false) }
    var step by remember { mutableIntStateOf(0) }
    var askFor by remember { mutableStateOf<String?>(null) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (cooking) "Cooking" else "Recipe") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface),
                navigationIcon = { IconButton({ if (cooking) cooking = false else onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton({ if (saved == null) vm.saveRecipe(r, true) else vm.toggleFavorite(saved) }) {
                        Icon(if (saved?.favorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favourite", tint = cs.primary)
                    }
                })
        },
        snackbarHost = { SnackbarHost(snack) },
    ) { pad ->
        AnimatedContent(cooking, label = "mode") { isCooking ->
            if (isCooking) CookMode(r.steps, step, { step = it }, onDone = { saved?.let(vm::markCooked); cooking = false; step = 0; scope.launch { snack.showSnackbar("Enjoy! Marked as cooked.") } }, Modifier.padding(pad))
            else Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Clover8Leaf) {
                    val on = cs.onPrimary
                    Text(r.title, style = MaterialTheme.typography.headlineMedium, color = on)
                    Text(r.description, color = on.copy(alpha = 0.85f))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                        Pill(Icons.Default.Schedule, "${r.minutes} min"); Pill(Icons.Default.LocalFireDepartment, "${r.calories_per_serving} kcal/serving"); Pill(Icons.Default.Check, "${r.servings} servings · ${r.difficulty}")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { cooking = true; step = 0 }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f).height(52.dp)) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.size(6.dp)); Text("Start cooking") }
                    if (r.missing.isNotEmpty()) FilledTonalButton(onClick = { vm.addMissingToGrocery(r); scope.launch { snack.showSnackbar("${r.missing.size} items added to grocery list") } }, shapes = ButtonDefaults.shapes(), modifier = Modifier.height(52.dp)) {
                        Icon(Icons.Default.AddShoppingCart, null); Spacer(Modifier.size(6.dp)); Text("Buy ${r.missing.size}")
                    }
                }
                StatCard {
                    Label("Ingredients")
                    r.ingredients.forEach { ing ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(10.dp).background(if (ing.have) cs.primary else cs.error, CircleShape))
                            Text(ing.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            Text(ing.amount, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                            if (!ing.have) TextButton({ askFor = ing.name; vm.askSubstitute(r, ing.name) }) { Text("Swap?") }
                        }
                    }
                }
                StatCard {
                    Label("Method")
                    r.steps.forEachIndexed { i, s ->
                        Row(Modifier.padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ShapeIcon(Icons.Default.Check, cs.secondaryContainer, cs.onSecondaryContainer, MaterialShapes.Cookie6Sided, 32)
                            Column { Text("Step ${i + 1}", style = MaterialTheme.typography.labelLarge, color = cs.primary); Text(s, style = MaterialTheme.typography.bodyLarge) }
                        }
                    }
                }
                if (saved == null) OutlinedButton(onClick = { vm.saveRecipe(r) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth()) { Text("Save to my recipes") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    askFor?.let { name ->
        AlertDialog(onDismissRequest = { askFor = null; vm.clearSubstitute() }, title = { Text("Instead of $name") },
            text = { when (val s = sub) { Job.Loading -> Row(verticalAlignment = Alignment.CenterVertically) { LoadingIndicator(); Spacer(Modifier.size(10.dp)); Text("Thinking…") }
                is Job.Done -> Text(s.value); is Job.Failed -> Text(s.message, color = cs.error); Job.Idle -> Text("") } },
            confirmButton = { TextButton({ askFor = null; vm.clearSubstitute() }) { Text("Got it") } })
    }
}

@Composable
private fun Pill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    val on = MaterialTheme.colorScheme.onPrimary
    Row(Modifier.height(30.dp).background(on.copy(alpha = 0.14f), MaterialTheme.shapes.large).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(icon, null, tint = on, modifier = Modifier.size(14.dp)); Text(text, style = MaterialTheme.typography.labelMedium, color = on)
    }
}

@Composable
private fun CookMode(steps: List<String>, step: Int, onStep: (Int) -> Unit, onDone: () -> Unit, modifier: Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LinearWavyProgressIndicator(progress = { (step + 1f) / steps.size }, modifier = Modifier.fillMaxWidth())
            Label("Step ${step + 1} of ${steps.size}")
            Text(steps.getOrElse(step) { "" }, style = MaterialTheme.typography.headlineMedium, color = cs.onSurface)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onStep(step - 1) }, enabled = step > 0, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f).height(56.dp)) { Text("Back") }
            Button(onClick = { if (step + 1 >= steps.size) onDone() else onStep(step + 1) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(2f).height(56.dp)) {
                Text(if (step + 1 >= steps.size) "Done — serve!" else "Next step", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
