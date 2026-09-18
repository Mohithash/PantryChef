package com.mohithash.pantrychef

import android.app.Application
import androidx.room.Room
import com.mohithash.pantrychef.ai.AiClient
import com.mohithash.pantrychef.ai.ChefAi
import com.mohithash.pantrychef.data.AppDb
import com.mohithash.pantrychef.data.JsonStore

class App : Application() {
    lateinit var db: AppDb
    lateinit var store: JsonStore
    val client = AiClient()
    val chef by lazy { ChefAi(client) }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDb::class.java, "pantrychef.db").build()
        store = JsonStore(this)
    }
}
