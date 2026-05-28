package com.operit.wechatcontact.data.database

import android.content.ContentValues
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.TypeMapping
import androidx.room.TypeProcessor
import com.operit.wechatcontact.data.dao.ContactDao
import com.operit.wechatcontact.data.model.Contact
import com.operit.wechatcontact.data.model.ContactIdentifier
import com.operit.wechatcontact.data.model.ContactStatus
import com.operit.wechatcontact.data.model.IdentifierType
import com.operit.wechatcontact.data.model.OperationLog

@Database(
    entities = [Contact::class, ContactIdentifier::class, OperationLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wechat_contact_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun fromIdentifierType(type: IdentifierType): String {
        return type.name
    }

    @androidx.room.TypeConverter
    fun toIdentifierType(value: String): IdentifierType {
        return IdentifierType.valueOf(value)
    }

    @androidx.room.TypeConverter
    fun fromContactStatus(status: ContactStatus): String {
        return status.name
    }

    @androidx.room.TypeConverter
    fun toContactStatus(value: String): ContactStatus {
        return ContactStatus.fromString(value)
    }
}