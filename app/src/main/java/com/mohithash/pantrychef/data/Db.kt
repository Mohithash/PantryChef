package com.mohithash.pantrychef.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pantry")
data class PantryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val qty: String = "",
    val category: String = "Other",
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "recipes")
data class RecipeRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** Full recipe JSON (see domain.Recipe). */
    val json: String,
    val favorite: Boolean = false,
    val cookedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "grocery")
data class GroceryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val forRecipe: String = "",
    val done: Boolean = false,
)

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry ORDER BY category, name") fun all(): Flow<List<PantryItem>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(items: List<PantryItem>)
    @Query("DELETE FROM pantry WHERE id = :id") suspend fun delete(id: Long)
    @Query("DELETE FROM pantry") suspend fun clear()
    @Query("SELECT name FROM pantry") suspend fun names(): List<String>
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY favorite DESC, createdAt DESC") fun all(): Flow<List<RecipeRow>>
    @Insert suspend fun insert(r: RecipeRow): Long
    @Update suspend fun update(r: RecipeRow)
    @Query("DELETE FROM recipes WHERE id = :id") suspend fun delete(id: Long)
    @Query("SELECT * FROM recipes WHERE id = :id") suspend fun get(id: Long): RecipeRow?
}

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery ORDER BY done, id") fun all(): Flow<List<GroceryItem>>
    @Insert suspend fun insert(items: List<GroceryItem>)
    @Update suspend fun update(g: GroceryItem)
    @Query("DELETE FROM grocery WHERE done = 1") suspend fun clearDone()
    @Query("DELETE FROM grocery WHERE id = :id") suspend fun delete(id: Long)
}

@Database(entities = [PantryItem::class, RecipeRow::class, GroceryItem::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun pantry(): PantryDao
    abstract fun recipes(): RecipeDao
    abstract fun grocery(): GroceryDao
}
