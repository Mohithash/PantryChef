package com.mohithash.pantrychef.ai

import com.mohithash.pantrychef.domain.Preferences
import com.mohithash.pantrychef.domain.Recipe
import com.mohithash.pantrychef.domain.RecipeBatch
import com.mohithash.pantrychef.domain.ScanResult

class ChefAi(private val client: AiClient) {
    private val recipeSchema = Schema.obj(
        "recipes" to Schema.arr(Schema.obj(
            "title" to Schema.str, "description" to Schema.str, "cuisine" to Schema.str, "minutes" to Schema.int,
            "servings" to Schema.int, "difficulty" to Schema.enum("easy", "medium", "hard"),
            "ingredients" to Schema.arr(Schema.obj("name" to Schema.str, "amount" to Schema.str, "have" to Schema.bool)),
            "steps" to Schema.arr(Schema.str), "calories_per_serving" to Schema.int, "tags" to Schema.arr(Schema.str),
        ))
    )
    private val scanSchema = Schema.obj("items" to Schema.arr(Schema.obj("name" to Schema.str, "qty" to Schema.str,
        "category" to Schema.enum("Produce", "Protein", "Dairy", "Grains", "Pantry", "Spices", "Frozen", "Other"))))

    private fun prefText(p: Preferences) = buildString {
        if (p.diet.isNotBlank()) append("Diet: ${p.diet}. ")
        if (p.allergies.isNotBlank()) append("Allergies (must avoid): ${p.allergies}. ")
        if (p.cuisine.isNotBlank()) append("Preferred cuisines: ${p.cuisine}. ")
        append("Servings: ${p.servings}. Max cooking time: ${p.maxMinutes} minutes.")
    }

    suspend fun suggest(s: AiSettings, pantry: List<String>, prefs: Preferences, mood: String): List<Recipe> {
        val system = """You are a resourceful home chef. Propose 4 distinct recipes that mostly use the pantry the user has.
            |Mark each ingredient with have=true if it is in the pantry (or a basic staple like salt, oil, water) and have=false otherwise.
            |Prefer recipes with few missing ingredients. Steps must be concrete, numbered in order, each one sentence or two.
            |Assume common staples (salt, pepper, oil, water) are available. ${prefText(prefs)}""".trimMargin()
        val user = "Pantry: ${pantry.joinToString(", ").ifBlank { "(empty — suggest simple recipes from cheap staples)" }}." +
            if (mood.isNotBlank()) " I'm in the mood for: $mood." else ""
        return client.ask<RecipeBatch>(s, system, user, recipeSchema, maxTokens = 8000).recipes
    }

    suspend fun scan(s: AiSettings, imageBase64: String?, text: String): ScanResult {
        val system = "You catalogue groceries. List every distinct food item you can identify with an approximate quantity and a category."
        val user = text.ifBlank { "Catalogue the food in this photo." }
        return client.ask(s, system, user, scanSchema, imageBase64, 4000)
    }

    suspend fun substitute(s: AiSettings, recipe: Recipe, missing: String): String =
        client.chat(s, "You are a pragmatic chef. Answer in 2-3 short sentences.",
            listOf(ChatMsg("user", "For the recipe '${recipe.title}', what can I use instead of '$missing'? Ingredients on hand: ${recipe.ingredients.filter { it.have }.joinToString { it.name }}")), maxTokens = 400)
}
