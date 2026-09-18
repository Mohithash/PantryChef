package com.mohithash.pantrychef.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohithash.pantrychef.App
import com.mohithash.pantrychef.ai.AiSettings
import com.mohithash.pantrychef.data.GroceryItem
import com.mohithash.pantrychef.data.PantryItem
import com.mohithash.pantrychef.data.RecipeRow
import com.mohithash.pantrychef.domain.Preferences
import com.mohithash.pantrychef.domain.Recipe
import com.mohithash.pantrychef.domain.ScannedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed interface Job<out T> {
    data object Idle : Job<Nothing>
    data object Loading : Job<Nothing>
    data class Done<T>(val value: T) : Job<T>
    data class Failed(val message: String) : Job<Nothing>
}

class AppViewModel(private val app: App) : ViewModel() {
    private val db = app.db
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    val client get() = app.client

    val ai: StateFlow<AiSettings> = app.store.flow("ai", AiSettings.serializer(), AiSettings())
    val prefs: StateFlow<Preferences> = app.store.flow("prefs", Preferences.serializer(), Preferences())
    fun saveAi(a: AiSettings) = app.store.set("ai", AiSettings.serializer(), a)
    fun savePrefs(p: Preferences) = app.store.set("prefs", Preferences.serializer(), p.copy(onboarded = true))

    val pantry = db.pantry().all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recipes = db.recipes().all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val grocery = db.grocery().all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _suggest = MutableStateFlow<Job<List<Recipe>>>(Job.Idle)
    val suggest: StateFlow<Job<List<Recipe>>> = _suggest
    private val _scan = MutableStateFlow<Job<List<ScannedItem>>>(Job.Idle)
    val scan: StateFlow<Job<List<ScannedItem>>> = _scan
    private val _sub = MutableStateFlow<Job<String>>(Job.Idle)
    val substitution: StateFlow<Job<String>> = _sub

    fun addPantry(name: String, qty: String, category: String) = viewModelScope.launch {
        if (name.isNotBlank()) db.pantry().insert(listOf(PantryItem(name = name.trim(), qty = qty.trim(), category = category)))
    }
    fun removePantry(id: Long) = viewModelScope.launch { db.pantry().delete(id) }

    fun scanPantry(image: String?, text: String) {
        _scan.value = Job.Loading
        viewModelScope.launch {
            _scan.value = runCatching { app.chef.scan(ai.value, image, text).items }.fold({ Job.Done(it) }, { Job.Failed(it.message ?: "Scan failed") })
        }
    }
    fun acceptScan(items: List<ScannedItem>) = viewModelScope.launch {
        db.pantry().insert(items.map { PantryItem(name = it.name, qty = it.qty, category = it.category) }); _scan.value = Job.Idle
    }
    fun clearScan() { _scan.value = Job.Idle }

    fun suggestRecipes(mood: String) {
        _suggest.value = Job.Loading
        viewModelScope.launch {
            val names = db.pantry().names()
            _suggest.value = runCatching { app.chef.suggest(ai.value, names, prefs.value, mood) }.fold({ Job.Done(it) }, { Job.Failed(it.message ?: "Failed") })
        }
    }
    fun clearSuggestions() { _suggest.value = Job.Idle }

    fun saveRecipe(r: Recipe, favorite: Boolean = false) = viewModelScope.launch {
        db.recipes().insert(RecipeRow(title = r.title, json = json.encodeToString(Recipe.serializer(), r), favorite = favorite))
    }
    fun toggleFavorite(row: RecipeRow) = viewModelScope.launch { db.recipes().update(row.copy(favorite = !row.favorite)) }
    fun markCooked(row: RecipeRow) = viewModelScope.launch { db.recipes().update(row.copy(cookedCount = row.cookedCount + 1)) }
    fun deleteRecipe(id: Long) = viewModelScope.launch { db.recipes().delete(id) }
    fun decode(row: RecipeRow): Recipe = json.decodeFromString(Recipe.serializer(), row.json)

    fun addMissingToGrocery(r: Recipe) = viewModelScope.launch {
        db.grocery().insert(r.missing.map { GroceryItem(name = listOf(it.amount, it.name).filter { s -> s.isNotBlank() }.joinToString(" "), forRecipe = r.title) })
    }
    fun addGrocery(name: String) = viewModelScope.launch { if (name.isNotBlank()) db.grocery().insert(listOf(GroceryItem(name = name.trim()))) }
    fun toggleGrocery(g: GroceryItem) = viewModelScope.launch { db.grocery().update(g.copy(done = !g.done)) }
    fun clearDone() = viewModelScope.launch { db.grocery().clearDone() }
    fun deleteGrocery(id: Long) = viewModelScope.launch { db.grocery().delete(id) }

    /** Recipe currently open in the detail screen (either a suggestion or a saved row). */
    val selected = MutableStateFlow<Pair<Recipe, RecipeRow?>?>(null)
    fun open(r: Recipe, row: RecipeRow? = null) { selected.value = r to row }

    fun askSubstitute(r: Recipe, missing: String) {
        _sub.value = Job.Loading
        viewModelScope.launch { _sub.value = runCatching { app.chef.substitute(ai.value, r, missing) }.fold({ Job.Done(it) }, { Job.Failed(it.message ?: "Failed") }) }
    }
    fun clearSubstitute() { _sub.value = Job.Idle }
}
