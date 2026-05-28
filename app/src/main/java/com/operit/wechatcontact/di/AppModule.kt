package com.operit.wechatcontact.di

import android.content.Context
import com.operit.wechatcontact.data.dao.ContactDao
import com.operit.wechatcontact.data.database.AppDatabase
import com.operit.wechatcontact.data.importer.ContactImporter
import com.operit.wechatcontact.data.repository.ContactRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideContactDao(database: AppDatabase): ContactDao {
        return database.contactDao()
    }

    @Provides
    @Singleton
    fun provideContactRepository(dao: ContactDao): ContactRepository {
        return ContactRepository(dao)
    }

    @Provides
    @Singleton
    fun provideContactImporter(@ApplicationContext context: Context, dao: ContactDao): ContactImporter {
        return ContactImporter(context, dao)
    }
}