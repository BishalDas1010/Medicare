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
/*
@Module
 This annotation tells Hilt:
"This class contains instructions for providing dependencies."
A dependency is an object that another class needs.
For example, your ViewModel needs MedicareDao to access the database.
Hilt needs to know how to create that DAO. Your module explains how.
Without @Module, Hilt would not recognize this object as a module containing provider functions.
 */
@Module

/*
This annotation tells Hilt where to install your module.
Let's understand the parts.
@InstallIn means:
"Install this module into a Hilt component."
SingletonComponent::class means:
"Install it into the application-level component."
The ::class syntax gets the Kotlin class reference.
So this annotation means:
Make the dependencies provided by this module available through the application-level Hilt component.
This is suitable for your Room database because you generally want it shared throughout your app.
*/
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /*
    @ provide  job is to provide the DAO to each screens so that they can do actions (CURD)*/
    @Provides
    /*
    @Singleton means: Hilt should create only one instance of this dependency in the component
    and reuse that same instance whenever it is requested.
    */
    @Singleton
    fun provideAppDatabase(
        /*
        This tells Hilt:

"I need the application context."

Android has different kinds of contexts, such as an Activity context and an Application context.

The application context belongs to the whole app rather than one particular screen.

Your database should generally use the application context because it may be used by multiple screens.
        */
        @ApplicationContext
        /*
    This is a function parameter.
    - context is the variable name.
    - Context is its type.
    You are saying:

    "This function needs a Context object."

    But you don't manually create it here.

    Because you used @ApplicationContext, Hilt knows which context to pass into the function.

    So when Hilt calls your function, it automatically supplies the application context.

    */
        context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    /*Its job is to provide the DAO to each screens so that they can do actions (CURD)*/
    @Provides
    fun provideMedicareDao(database: AppDatabase): MedicareDao {
        return database.medicareDao()
    }
}
