package com.example.madicare.DATABASE.HiltModules

import android.content.Context
import com.example.madicare.DATABASE.AppDatabase
import com.example.madicare.DATABASE.MedicareDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideMedicareDao(database: AppDatabase): MedicareDao {
        return database.medicareDao()
    }
}
