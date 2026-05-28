package com.operit.wechatcontact

import android.app.Application
import com.operit.wechatcontact.data.database.AppDatabase

class WeChatApp : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }
}