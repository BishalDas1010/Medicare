package com.example.madicare.DATABASE.HiltModules

import android.content.Context
import androidx.room.Room
import com.example.madicare.DATABASE.AppDatabase
import com.example.madicare.DATABASE.MedicareDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
//@MOdule: Hilt cannot automatically understand how to create a Room database
// because Room requires a Context, database class and database name.
//So you tell Hilt how to create it inside this module.
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun ProvideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase{
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "medicare_databse"
        ).fallbackToDestructiveMigration().build()

        // NOTE : Suppose your MediCare database has medicine records
        //You update the database schema from version 1 to version 2 without providing a migration.
        //This method can delete the existing database and recreate it.
        //For a real MediCare production app, avoid this unless data
        // loss is acceptable. Implement proper Room migrations instead.
    }



    @Provides
    fun provideMedicareDao(database: AppDatabase): MedicareDao{
        return  database.medicareDao()
    }

}