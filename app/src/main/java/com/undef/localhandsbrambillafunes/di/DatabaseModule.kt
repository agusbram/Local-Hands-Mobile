package com.undef.localhandsbrambillafunes.di

import android.content.Context
import androidx.room.Room
import com.undef.localhandsbrambillafunes.data.dao.FavoriteDao
import com.undef.localhandsbrambillafunes.data.dao.ProductDao
import com.undef.localhandsbrambillafunes.data.dao.SellerDao
import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
            .addCallback(callback)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideSellerDao(db: AppDatabase): SellerDao = db.sellerDao()
}
