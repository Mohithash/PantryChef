package com.mohithash.pantrychef.domain

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(val name: String, val amount: String = "", val have: Boolean = true)

@Serializable
data class Recipe(
    val title: String,
    val description: String = "",
    val cuisine: String = "",
    val minutes: Int = 30,
    val servings: Int = 2,
    val difficulty: String = "easy",
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val calories_per_serving: Int = 0,
    val tags: List<String> = emptyList(),
) {
    val missing get() = ingredients.filter { !it.have }
}

@Serializable data class RecipeBatch(val recipes: List<Recipe> = emptyList())

@Serializable data class ScannedItem(val name: String, val qty: String = "", val category: String = "Other")
@Serializable data class ScanResult(val items: List<ScannedItem> = emptyList())

@Serializable
data class Preferences(
    val diet: String = "",
    val allergies: String = "",
    val cuisine: String = "",
    val servings: Int = 2,
    val maxMinutes: Int = 45,
    val onboarded: Boolean = false,
)

val CATEGORIES = listOf("Produce", "Protein", "Dairy", "Grains", "Pantry", "Spices", "Frozen", "Other")
