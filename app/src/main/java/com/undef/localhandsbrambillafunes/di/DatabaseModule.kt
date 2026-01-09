package com.undef.localhandsbrambillafunes.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.undef.localhandsbrambillafunes.data.dao.FavoriteDao
import com.undef.localhandsbrambillafunes.data.dao.ProductDao
import com.undef.localhandsbrambillafunes.data.dao.SellerDao
import com.undef.localhandsbrambillafunes.data.dao.UserDao
import com.undef.localhandsbrambillafunes.data.db.AppDatabase
import com.undef.localhandsbrambillafunes.data.entity.Product
import com.undef.localhandsbrambillafunes.data.entity.Seller
import com.undef.localhandsbrambillafunes.data.entity.User
import com.undef.localhandsbrambillafunes.data.entity.UserRole
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        callback: Provider<AppDatabaseCallback>
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
            // Solución a la advertencia de deprecación
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(callback.get())
            .build()
    }

    @Provides
    @Singleton
    fun provideAppDatabaseCallback(
        db: Provider<AppDatabase>,
        coroutineScope: CoroutineScope
    ): AppDatabaseCallback {
        return AppDatabaseCallback(db, coroutineScope)
    }

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO)
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideSellerDao(database: AppDatabase): SellerDao = database.sellerDao()
}

class AppDatabaseCallback(
    private val database: Provider<AppDatabase>,
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch {
            populateDatabase()
        }
    }

    private suspend fun populateDatabase() {
        val userDao = database.get().userDao()
        val sellerDao = database.get().sellerDao()
        val productDao = database.get().productDao()

        // Limpieza inicial
        userDao.deleteAllUsers()
        sellerDao.deleteAll()

        // --- VENDEDOR 1: Huerta de Ana ---
        val user1Id = userDao.insertUser(
            User(
                name = "Ana",
                lastName = "García",
                email = "ana.garcia@huerta.com",
                password = "hashed_password_1", // En un caso real, esto debería estar hasheado
                phone = "341-1234567",
                address = "Av. Siempre Viva 742, Rosario",
                role = UserRole.SELLER
            )
        )

        sellerDao.insertSeller(
            Seller(
                id = user1Id.toInt(),
                name = "Ana",
                lastname = "García",
                email = "ana.garcia@huerta.com",
                phone = "341-1234567",
                entrepreneurship = "Huerta de Ana",
                address = "Av. Siempre Viva 742, Rosario"
            )
        )

        // --- VENDEDOR 2: Cerámicas de la Ribera ---
        val user2Id = userDao.insertUser(
            User(
                name = "Carlos",
                lastName = "Pérez",
                email = "carlos.perez@ceramica.com",
                password = "hashed_password_2",
                phone = "3436-987654",
                address = "Calle Falsa 123, Victoria",
                role = UserRole.SELLER
            )
        )

        sellerDao.insertSeller(
            Seller(
                id = user2Id.toInt(),
                name = "Carlos",
                lastname = "Pérez",
                email = "carlos.perez@ceramica.com",
                phone = "3436-987654",
                entrepreneurship = "Cerámicas de la Ribera",
                address = "Calle Falsa 123, Victoria"
            )
        )

        // --- PRODUCTOS DE MUESTRA ---
        productDao.insertAll(
            listOf(
                Product(
                    name = "Canasta de Verduras de Estación",
                    description = "Una selección de 5kg de las mejores verduras de la semana.",
                    producer = "Huerta de Ana",
                    category = "Alimentos",
                    ownerId = user1Id.toInt(),
                    images = listOf("https://i.imgur.com/xOJDC5L.jpeg"),
                    price = 1500.00,
                    location = "Rosario"
                ),
                Product(
                    name = "Tomates Cherry Orgánicos (500g)",
                    description = "Dulces y jugosos, cultivados sin pesticidas.",
                    producer = "Huerta de Ana",
                    category = "Alimentos",
                    ownerId = user1Id.toInt(),
                    images = listOf("https://i.imgur.com/7P1P2uW.jpeg"),
                    price = 450.00,
                    location = "Rosario"
                ),
                Product(
                    name = "Tazón de cerámica 'Río'",
                    description = "Tazón de 300ml esmaltado en tonos azules y marrones.",
                    producer = "Cerámicas de la Ribera",
                    category = "Artesanías",
                    ownerId = user2Id.toInt(),
                    images = listOf("https://i.imgur.com/HnK2b2e.jpeg"),
                    price = 1200.00,
                    location = "Victoria"
                )
            )
        )
    }
}
